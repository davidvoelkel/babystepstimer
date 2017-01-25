/*  Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package net.davidtanzer.babysteps;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class BabystepsTimer {
	private static final String BACKGROUND_COLOR_NEUTRAL = "#ffffff";
	private static final String BACKGROUND_COLOR_FAILED = "#ffcccc";
	private static final String BACKGROUND_COLOR_PASSED = "#ccffcc";

	private static final long SECONDS_IN_CYCLE = 120;

	private static JFrame timerFrame;
	private static JTextPane timerPane;
	private static boolean timerRunning;
	private static long currentCycleStartTime;
	private static String lastRemainingTime;
	private static String bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
	private static List<String> logLines;
	public static List<String> getLogLines() {
		return logLines;
	}

	private static Clock clock = new RealClock();
	private static QuitHandler quitHandler = () -> System.exit(0);
	
	static void setQuitHandler(QuitHandler quitHandler) {
		BabystepsTimer.quitHandler = quitHandler;
	}

	static void setClock(Clock clock) {
		BabystepsTimer.clock = clock;
	}

	private static DecimalFormat twoDigitsFormat = new DecimalFormat("00");

	public static void main(final String[] args) {
		logLines = new ArrayList<>();
		setBackgroundColor(BACKGROUND_COLOR_NEUTRAL);
		timerFrame = new JFrame("Babysteps Timer");
		timerFrame.setUndecorated(true);

		timerFrame.setSize(250, 120);
		timerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		timerPane = new JTextPane();
		timerPane.setContentType("text/html");
		createTimerHtml(getRemainingTimeCaption(0L), BACKGROUND_COLOR_NEUTRAL, false);
		timerPane.setEditable(false);
		timerPane.addMouseMotionListener(new MouseMotionListener() {
			private int lastX;
			private int lastY;

			@Override
			public void mouseMoved(final MouseEvent e) {
				lastX = e.getXOnScreen();
				lastY = e.getYOnScreen();
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				int x = e.getXOnScreen();
				int y = e.getYOnScreen();
				
				timerFrame.setLocation(timerFrame.getLocation().x + (x-lastX), timerFrame.getLocation().y + (y-lastY));
				
				lastX = x;
				lastY = y;
			}
		});
		timerPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if("command://start".equals(e.getDescription())) {
						start();
					} else if("command://stop".equals(e.getDescription())) {
						stop();
					} else  if("command://reset".equals(e.getDescription())) {
						reset();
					} else  if("command://quit".equals(e.getDescription())) {
						quit();
					}
				}
			}

		});
		timerFrame.getContentPane().add(timerPane);

		timerFrame.setVisible(true);
	}
	
	private static void alwaysOnTop(boolean alwaysOnTop) {
		log("alwaysOnTop=" + alwaysOnTop);
		timerFrame.setAlwaysOnTop(alwaysOnTop);
	}
	
	private static void repaint() {
		log("repaint");
		timerFrame.repaint();
	}

	private static String getRemainingTimeCaption(final long elapsedTime) {
		long elapsedSeconds = elapsedTime/1000;
		long remainingSeconds = SECONDS_IN_CYCLE - elapsedSeconds;
		
		long remainingMinutes = remainingSeconds/60;
		return twoDigitsFormat.format(remainingMinutes)+":"+twoDigitsFormat.format(remainingSeconds-remainingMinutes*60);
	}

	private static void createTimerHtml(final String timerText, final String bodyColor, final boolean running) {
		log("timerText=" + timerText + ", bodyColor=" + bodyColor + ", running=" + running);
		String timerHtml = "<html><body style=\"border: 3px solid #555555; background: "+bodyColor+"; margin: 0; padding: 0;\">" +
				"<h1 style=\"text-align: center; font-size: 30px; color: #333333;\">"+timerText+"</h1>" +
				"<div style=\"text-align: center\">";
		if(running) {
			timerHtml += "<a style=\"color: #555555;\" href=\"command://stop\">Stop</a> " +
					"<a style=\"color: #555555;\" href=\"command://reset\">Reset</a> ";
		} else {
			timerHtml += "<a style=\"color: #555555;\" href=\"command://start\">Start</a> ";
		}
		timerHtml += "<a style=\"color: #555555;\" href=\"command://quit\">Quit</a> ";
		timerHtml += "</div>" +
				"</body></html>";
		timerPane.setText(timerHtml);
	}

	private static void log(String message) {
		System.out.println(message);
		logLines.add(message);
	}

	public static synchronized void playSound(final String url) {
		log("playSound=" + url);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Clip clip = AudioSystem.getClip();
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(
							BabystepsTimer.class.getResourceAsStream("/"+url));
					clip.open(inputStream);
					clip.start();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}).start();
	}

	private static String setBackgroundColor(String color) {
		log("backgroundColor=" + color);
		return bodyBackgroundColor=color;
	}

	private static long now() {
		return clock.now();
	}

	static void start() {
		alwaysOnTop(true);
		createTimerHtml(getRemainingTimeCaption(0L), BACKGROUND_COLOR_NEUTRAL, true);
		repaint();
		new TimerThread().start();
	}

	static void stop() {
		timerRunning = false;
		alwaysOnTop(false);
		createTimerHtml(getRemainingTimeCaption(0L), BACKGROUND_COLOR_NEUTRAL, false);
		repaint();
	}

	static void reset() {
		currentCycleStartTime = now();
		setBackgroundColor(BACKGROUND_COLOR_PASSED);
	}

	static void quit() {
		log("exit");
		quitHandler.onQuit();
	}

	private static final class TimerThread extends Thread {
		@Override
		public void run() {
			timerRunning = true;
			currentCycleStartTime = now();
			
			while(timerRunning) {
				long elapsedTime = now() - currentCycleStartTime;
				
				if(elapsedTime >= SECONDS_IN_CYCLE*1000+980) {
					currentCycleStartTime = now();
					elapsedTime = now() - currentCycleStartTime;
				}
				if(elapsedTime >= 5000 && elapsedTime < 6000 && !BACKGROUND_COLOR_NEUTRAL.equals(bodyBackgroundColor)) {
					setBackgroundColor(BACKGROUND_COLOR_NEUTRAL);
				}
				
				String remainingTime = getRemainingTimeCaption(elapsedTime);
				if(!remainingTime.equals(lastRemainingTime)) {
					if(remainingTime.equals("00:10")) {
						playSound("2166__suburban-grilla__bowl-struck.wav");
					} else if(remainingTime.equals("00:00")) {
						playSound("32304__acclivity__shipsbell.wav");
						setBackgroundColor(BACKGROUND_COLOR_FAILED);
					}
					
					createTimerHtml(remainingTime, bodyBackgroundColor, true);
					repaint();
					lastRemainingTime = remainingTime;
				}
				clock.sleep(10);
			}
		}
	}
}

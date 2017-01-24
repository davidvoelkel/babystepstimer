package net.davidtanzer.babysteps;

final class RealClock implements Clock {
	@Override
	public void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			//We don't really care about this one...
		}
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}
}
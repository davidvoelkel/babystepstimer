package net.davidtanzer.babysteps;

public interface Clock {

	void sleep(int millis);

	long now();

}

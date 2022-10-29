package nbradham.deliciousforce;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Random;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

/**
 * Handles everything.
 * 
 * @author Nickolas Bradham
 *
 */
public final class Forcer implements NativeKeyListener {

	private static final short BOX_X = 680, BOX_Y = 120, BOX_W = 750, BOX_H = 610, FAIL_X = 640, FAIL_Y = 450;
	private final Robot ro;
	private final Object lock = new Object();
	private boolean running = true, clicking = false;

	/**
	 * Constructs a new Forcer.
	 * 
	 * @throws AWTException Thrown by {@link Robot#Robot()}.
	 */
	private Forcer() throws AWTException {
		ro = new Robot();
	}

	/**
	 * Handles main execution loop.
	 * 
	 * @throws NativeHookException  Thrown by
	 *                              {@link GlobalScreen#registerNativeHook()} and
	 *                              {@link GlobalScreen#unregisterNativeHook()}.
	 * @throws InterruptedException Thrown by {@link #wait()} and
	 *                              {@link #click(int, int)}.
	 */
	private void start() throws NativeHookException, InterruptedException {
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(this);
		Random ra = new Random();
		while (running) {
			synchronized (lock) {
				lock.wait();
			}
			while (clicking) {
				click(BOX_X + ra.nextInt(BOX_W), BOX_Y + ra.nextInt(BOX_H));
				while (ro.getPixelColor(FAIL_X, FAIL_Y).equals(Color.BLACK))
					click(FAIL_X, FAIL_Y);
			}
		}
		GlobalScreen.unregisterNativeHook();
	}

	/**
	 * Moves cursor to (x, y) and clicks.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @throws InterruptedException Thrown by {@link Thread#sleep(long)}.
	 */
	private void click(int x, int y) throws InterruptedException {
		ro.mouseMove(x, y);
		ro.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		Thread.sleep(1);
		ro.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
		switch (nativeEvent.getKeyCode()) {
		case NativeKeyEvent.VC_F:
			clicking = !clicking;
			break;
		case NativeKeyEvent.VC_Q:
			clicking = false;
			running = false;
		}
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * Constructs and starts a Forcer instance.
	 * 
	 * @param args Ignored.
	 * @throws NativeHookException  Thrown by {@link #start()}.
	 * @throws InterruptedException Thrown by {@link #start()}.
	 * @throws AWTException         Thrown by {@link #Forcer()}.
	 */
	public static void main(String[] args) throws NativeHookException, InterruptedException, AWTException {
		new Forcer().start();
	}
}
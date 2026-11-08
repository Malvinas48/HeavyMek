import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Tiny UI driver for testing on a virtual display. Run with: java RobotClick.java step [step ...]
 * Steps: "click:X:Y", "dclick:X:Y", "sleep:MS", "key:NAME" (ENTER, ESCAPE, TAB, SPACE), "type:TEXT".
 */
public class RobotClick {
    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        robot.setAutoDelay(40);
        for (String step : args) {
            String[] parts = step.split(":", 3);
            switch (parts[0]) {
                case "click" -> click(robot, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), 1);
                case "dclick" -> click(robot, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), 2);
                case "sleep" -> Thread.sleep(Long.parseLong(parts[1]));
                case "key" -> press(robot, (int) KeyEvent.class.getField("VK_" + parts[1]).get(null));
                case "type" -> {
                    for (char c : parts[1].toCharArray()) {
                        int code = KeyEvent.getExtendedKeyCodeForChar(c);
                        boolean shift = Character.isUpperCase(c);
                        if (shift) {
                            robot.keyPress(KeyEvent.VK_SHIFT);
                        }
                        press(robot, code);
                        if (shift) {
                            robot.keyRelease(KeyEvent.VK_SHIFT);
                        }
                    }
                }
                default -> throw new IllegalArgumentException("Unknown step " + step);
            }
        }
    }

    private static void click(Robot robot, int x, int y, int times) {
        robot.mouseMove(x, y);
        for (int i = 0; i < times; i++) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private static void press(Robot robot, int code) {
        robot.keyPress(code);
        robot.keyRelease(code);
    }
}

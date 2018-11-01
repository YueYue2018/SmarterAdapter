package github.com.smartadapter;

public class FastClickUtil {
    public static int time_shake = 500;//防快速点击
    private static long last_click_time = 0;

    public static boolean isCanClick() {
        if (System.currentTimeMillis() - last_click_time > time_shake) {
            last_click_time = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }
}

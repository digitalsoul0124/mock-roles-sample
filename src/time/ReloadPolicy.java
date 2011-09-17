package time;

public interface ReloadPolicy {

    boolean shouldReload(Timestamp loadTime, Timestamp fetchTime);

}

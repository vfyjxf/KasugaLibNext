package lib.kasuga.early;

public interface ModLoadingProgress {
    public void increment();
    public void complete();
    public void set(int i);
    public int steps();
    public void label(String title);
}

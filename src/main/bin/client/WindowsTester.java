public class WindowsTester {
    public static void main(String[] args) {
	int out = (System.getProperty("os.name").toLowerCase().startsWith("win")) ? 1 : 0;
	System.out.print(out);
    }
}

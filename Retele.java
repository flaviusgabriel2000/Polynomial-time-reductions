import java.io.IOException;

class Retele {
    /**
     * Calls the solver of this problem.
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     * @throws InterruptedException in case of thread interruption
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Task1 task1 = new Task1();
        task1.solve();
    }
}
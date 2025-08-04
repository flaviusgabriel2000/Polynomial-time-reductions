import java.io.IOException;

class Reclame {
    /**
     * Calls the solver of this problem.
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     * @throws InterruptedException in case of thread interruption
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Task2 task2 = new Task2();
        task2.solve();
    }
}
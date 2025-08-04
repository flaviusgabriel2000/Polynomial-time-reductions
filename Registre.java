import java.io.IOException;

class Registre {
    /**
     * Calls the solver of this problem.
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     * @throws InterruptedException in case of thread interruption
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Task3 task3 = new Task3();
        task3.solve();
    }
}
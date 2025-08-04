import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class Task1 extends Task {
    /**
     * The graph built for the current instance.
     */
    private final Graph g;
    /**
     * Number of members in the network.
     */
    private int N;
    /**
     * Number of friendship relations.
     */
    private int M;
    /**
     * Group dimension.
     */
    private int K;
    /**
     * Stores the extended group of people, after
     * deciphering the Oracle answer.
     */
    private final List<Integer> decipheredVertices;

    /**
     * Constructor
     */
    public Task1() {
        this.g = new Graph();
        this.decipheredVertices = new ArrayList<>();
    }

    /**
     * Solves the current instance.
     * @throws IOException in case of exceptions to reading / writing
     * @throws InterruptedException in case of thread interruption
     */
    @Override
    public void solve() throws IOException, InterruptedException {
        readProblemData();
        formulateOracleQuestion();
        askOracle();
        decipherOracleAnswer();
        writeAnswer();
    }

    /**
     * Reads the problem data from stdin and builds the graph.
     * @throws IOException in case of exceptions to reading / writing
     */
    @Override
    public void readProblemData() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String  line = input.readLine();
        String[] strings = line.trim().split("\\s+");
        N = Integer.parseInt(strings[0]);
        M = Integer.parseInt(strings[1]);
        K = Integer.parseInt(strings[2]);

        // Build the graph with data read from stdin
        for (int i = 1; i <= M; i++) {
            line = input.readLine();
            strings = line.trim().split("\\s+");
            g.addEdge(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
        }
        input.close();
    }

    /**
     * Performs a k-clique reduction to SAT and builds the .cnf file
     * needed by the Oracle.
     * @throws IOException in case of exceptions to reading / writing
     */
    @Override
    public void formulateOracleQuestion() throws IOException {
        // Prepare the .cnf file to be written with BufferedWriter
        File file = new File("sat.cnf");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter output = new BufferedWriter(fileWriter);
        StringBuilder stringBuilder = new StringBuilder();

        // Lists of clauses and encoded variables
        List<String> clauses = new ArrayList<>();
        List<Integer> variables = new ArrayList<>();
        int noOfClauses = 0;

        // Encode the N * K variables with numbers starting from 1.
        for (int i = 1; i <= N * K; i++) {
            variables.add(i);
        }

        try {
            // For every i, there exists and i-th vertex in the clique.
            for (int i = 1; i <= K; i++) {
                for (int v = 1; v <= N; v++) {
                    if (v == N) {
                        stringBuilder.append(variables.get(i - 1 + K * (v - 1))).append(" 0");
                    } else {
                        stringBuilder.append(variables.get(i - 1 + K * (v - 1))).append(" ");
                    }
                }
                clauses.add(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                noOfClauses++;
            }
            // For every non-edge (v, w), v and w cannot both be in the clique.
            for (int i = 1; i < K; i++) {
                for (int j = i + 1; j <= K; j++) {
                    for (int v = 1; v < N; v++) {
                        for (int w = v + 1; w <= N; w++) {
                            if (!g.hasEdge(v, w)) {
                                clauses.add(-variables.get(i - 1 + K * (v - 1)) + " "
                                        + (-variables.get(j - 1 + K * (w - 1)) + " 0"));
                                clauses.add(-variables.get(i - 1 + K * (w - 1)) + " "
                                        + (-variables.get(j - 1 + K * (v - 1)) + " 0"));
                                noOfClauses += 2;
                            }
                        }
                    }
                }
            }
            // For every i, j (where i != j), the i-th vertex is
            // different from the j-th vertex.
            for (int i = 1; i < K; i++) {
                for (int j = i + 1; j <= K; j++) {
                    for (int v = 1; v <= N; v++) {
                        clauses.add(-variables.get(i - 1 + K * (v - 1)) + " "
                                + (-variables.get(j - 1 + K * (v - 1))) + " 0");
                        noOfClauses++;
                    }
                }
            }
            // Build the .cnf file.
            output.write("p cnf" + " " + N * K + " " + noOfClauses + "\n");
            for (String s : clauses) {
                output.write(s + "\n");
            }
            output.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * Deciphers the Oracle answer for the current instance.
     * @throws IOException in case of exceptions to reading / writing
     */
    @Override
    public void decipherOracleAnswer() throws IOException {
        // Parse the .sol file generated by the Oracle.
        BufferedReader bufferedReader = new BufferedReader(new FileReader("sat.sol"));
        String line = bufferedReader.readLine();
        int noOfVariables;
        if (line.equalsIgnoreCase("True")) {
            line = bufferedReader.readLine();
            noOfVariables = Integer.parseInt(line);
            line = bufferedReader.readLine();

            String[] strings = line.trim().split("\\s+");
            List<Integer> oracleAnswer = new ArrayList<>();

            // We only need the positive values returned by the Oracle.
            for (int i = 0; i < noOfVariables; i++) {
                if (Integer.parseInt(strings[i]) > 0) {
                    oracleAnswer.add(Integer.parseInt(strings[i]));
                }
            }
            /*
                Considering the encoding of the variables which we applied,
                the vertices we are looking for are given by the division
                between the encoded variable and the group dimension.
             */
            for (Integer variable : oracleAnswer) {
                if (variable % K != 0) {
                    decipheredVertices.add(variable / K + 1);
                } else {
                    decipheredVertices.add(variable / K);
                }
            }
        }
        bufferedReader.close();
    }

    /**
     * Write the final answer for this instance to stdout.
     */
    @Override
    public void writeAnswer() {
        if (decipheredVertices.isEmpty()) {
            System.out.println("False");
        } else {
            // There's a clique of size K in this graph,
            // representing the extended group of people.
            System.out.println("True");
            for (int i = 0; i < decipheredVertices.size(); i++) {
                if (i == decipheredVertices.size() - 1) {
                    System.out.print(decipheredVertices.get(i));
                } else {
                    System.out.print(decipheredVertices.get(i) + " ");
                }
            }
            System.out.println();
        }
    }
}

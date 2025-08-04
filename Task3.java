import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Task3 extends Task {
    /**
     * The graph built for the current instance.
     */
    private final Graph g;
    /**
     * Number of variables.
     */
    private int N;
    /**
     * Number of relations between the variables.
     */
    private int M;
    /**
     * Number of available registers.
     */
    private int K;
    /**
     * HashMap of <Variable, AssignedRegister> pairs.
     */
    private final Map<Integer, Integer> decipheredVariablesMap;

    /**
     * Constructor
     */
    public Task3() {
        this.g = new Graph();
        this.decipheredVariablesMap = new HashMap<>();
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
     * Performs a k-coloring reduction to SAT and builds the .cnf file
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
            // Each vertex must be assigned one of the available K colours.
            for (int v = 1; v <= N; v++) {
                for (int i = 1; i <= K; i++) {
                    if (i == K) {
                        stringBuilder.append(variables.get(i - 1 + K * (v - 1))).append(" 0");
                    } else {
                        stringBuilder.append(variables.get(i - 1 + K * (v - 1))).append(" ");
                    }

                    noOfClauses++;
                }
                clauses.add(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
            }
            // A vertex can be assigned at most one of the available colours.
            for (int i = 1; i < K; i++) {
                for (int j = i + 1; j <= K; j++) {
                    for (int v = 1; v <= N; v++) {
                        clauses.add(-variables.get(i - 1 + K * (v - 1)) + " "
                                + (-variables.get(j - 1 + K * (v - 1))) + " 0");
                        noOfClauses++;
                    }
                }
            }
            // Two adjacent vertices cannot have the same colour.
            for (int i = 1; i <= K; i++) {
                for (int v = 1; v < N; v++) {
                    for (int w = v + 1; w <= N; w++) {
                        if(g.hasEdge(v, w)) {
                            clauses.add(-variables.get(i - 1 + K * (v - 1)) + " "
                                    + (-variables.get(i - 1 + K * (w - 1)) + " 0"));
                            noOfClauses++;
                        }
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
            int assignedRegister = 0;
            for (Integer variable : oracleAnswer) {
                // Compute the number of the register which needs to be
                // assigned to this variable. The registers are also
                // encoded with numbers from 1 to K.
                int count = 1;
                for (int i = 1; i <= N * K; i++) {
                    if (i == variable) {
                        assignedRegister = count;
                        break;
                    }
                    count++;
                    if (count == K + 1) {
                        count = 1;
                    }
                }
                /*
                Considering the encoding of the variables which we applied, the
                vertices we are looking for are given by the division between
                the encoded variable (of the CNF formula) and the group dimension.
                */
                if (variable % K != 0) {
                    decipheredVariablesMap.put(variable / K + 1, assignedRegister);
                } else {
                    decipheredVariablesMap.put(variable / K, assignedRegister);
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
        if (decipheredVariablesMap.size() != N) {
            System.out.println("False");
        } else {
            // This graph has a k-coloring, each colour representing the
            // number of the register assigned to every variable, according
            // to the Oracle answer.
            System.out.println("True");
            for (Map.Entry<Integer, Integer> entry : decipheredVariablesMap.entrySet()) {
                System.out.print(entry.getValue() + " ");
            }
            System.out.println();
        }
    }
}

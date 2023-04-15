import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class homework {

    public static class Literal {
        private String predicate;
        private List<String> arguments;
        private boolean negated;

        public Literal(String predicate, List<String> arguments, boolean negated) {
            this.predicate = predicate;
            this.arguments = arguments;
            this.negated = negated;
        }

        public String getPredicate() {
            return predicate;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public boolean isNegated() {
            return negated;
        }

        public void negate() {
            negated = !negated;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (negated) {
                sb.append("~");
            }
            sb.append(predicate);
            sb.append("(");
            for (int i = 0; i < arguments.size(); i++) {
                sb.append(arguments.get(i));
                if (i < arguments.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static class Clause {
        private List<Literal> literals;

        public Clause(List<Literal> literals) {
            this.literals = literals;
        }

        public List<Literal> getLiterals() {
            return literals;
        }

        public boolean containsLiteral(Literal literal) {
            for (Literal l : literals) {
                if (l.getPredicate().equals(literal.getPredicate()) && l.isNegated() == literal.isNegated()
                        && l.getArguments().size() == literal.getArguments().size()) {
                    boolean match = true;
                    for (int i = 0; i < l.getArguments().size(); i++) {
                        if (!l.getArguments().get(i).equals(literal.getArguments().get(i))) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void addLiteral(Literal literal) {
            if (!containsLiteral(literal)) {
                literals.add(literal);
            }
        }

        public boolean isEmpty() {
            return literals.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (int i = 0; i < literals.size(); i++) {
                sb.append(literals.get(i));
                if (i < literals.size() - 1) {
                    sb.append(" | ");
                }
            }
            sb.append("}");
            return sb.toString();
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("./src/input.txt"));

        PrintWriter output = new PrintWriter(new File("output.txt"));

        // Read in the query
        String query = sc.nextLine();

        // Read in the number of sentences in the knowledge base
        int numSentences = Integer.parseInt(sc.nextLine());

        // Initialize an empty list to hold the knowledge base sentences
        List<String> kbSentences = new ArrayList<>();

        // Read in each sentence in the knowledge base and add it to the list
        for (int i = 0; i < numSentences; i++) {
            String sentence = sc.nextLine();
            kbSentences.add(sentence);
        }

        // Convert the knowledge base sentences to CNF
        List<Clause> cnfSentences = new ArrayList<>();
        for (String sentence : kbSentences) {
            /*List<Clause> clause = convertToCNF(sentence);
            cnfSentences.addAll(clause);*/
        }

        System.out.println(negate("~GetCheck(x) | Paid(x)"));

        for(Clause cl : cnfSentences) {
            System.out.println(cl);
        }

        // Perform resolution with the query and the CNF knowledge base
        boolean result = resolution(query, cnfSentences);

        // Output the result
        output.print(result ? "TRUE" : "FALSE");

        sc.close();
        output.close();
    }

    public static List<Clause> convertToCNF(String sentence) {
        String disjunction;
        // Step 1: Rewrite implication as disjunction of negations
        if(sentence.contains("=>")) {
            String[] implicationParts = sentence.split("=>");
            disjunction = negate(implicationParts[0].trim()) + " | " + implicationParts[1].trim();
        }
        else {
            disjunction = sentence;
        }

        // Step 2: Move negations inward using De Morgan's laws
        String[] disjunctionParts = disjunction.split("\\|");
        for (int i = 0; i < disjunctionParts.length; i++) {
            String part = disjunctionParts[i].trim();
            if (part.startsWith("~")) {
                String predicate = part.substring(1);
                disjunctionParts[i] = "~" + predicate.replaceAll("\\(([^)]+)\\)", "($1)") + "";
            } else {
                disjunctionParts[i] = part.replaceAll("\\(([^)]+)\\)", "($1)");
            }
        }
        String conjunction = String.join(" & ", disjunctionParts);

        // Step 4: Group into conjunction of disjunctions
        String[] conjunctionParts = conjunction.split("&");
        List<Clause> clauses = new ArrayList<>();
        for (int i = 0; i < conjunctionParts.length; i++) {
            List<Literal> literals = new ArrayList<>();
            String part = conjunctionParts[i].trim();
            String predicate = part.substring(0, part.indexOf("("));
            List<String> arguments = Arrays.asList(part.substring(part.indexOf("(") + 1, part.indexOf(")")).split(","));
            boolean negated = false;
            if (predicate.startsWith("~")) {
                predicate = predicate.substring(1);
                negated = true;
            }
            literals.add(new Literal(predicate, arguments, negated));
            clauses.add(new Clause(literals));
        }

        return clauses;
    }

    private static String negate(String sentence) {
        if (sentence.contains("&")) {
            String[] conjuncts = sentence.split("&");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < conjuncts.length; i++) {
                if (i > 0) {
                    sb.append("|");
                }
                sb.append(negate(conjuncts[i]).trim());
            }
            return sb.toString();
        } else if (sentence.contains("|")) {
            String[] disjuncts = sentence.split("\\|");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < disjuncts.length; i++) {
                if (i > 0) {
                    sb.append("&");
                }
                sb.append(negate(disjuncts[i]).trim());
            }
            return sb.toString();
        } else {
            return sentence.startsWith("~") ? sentence.substring(1, sentence.length() - 1).trim() : "~" + sentence.trim();
        }
    }

    public static boolean resolution(String query, List<Clause> cnfSentences) {
        // TODO: Implement resolution algorithm
        return false;
    }
}
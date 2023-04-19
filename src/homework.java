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

        public boolean isComplementary(Literal lit) {
            return this.predicate.equals(lit.getPredicate()) && ((this.negated && !lit.isNegated()) || (!this.negated && lit.isNegated()));
        }

        public Literal getNegation() {
            return new Literal(this.predicate, this.arguments, !this.negated);
        }

        public boolean isUnifiable(Literal lit) {
            if (!this.predicate.equals(lit.getPredicate())) {
                return false;
            }

            List<String> litArgs = lit.getArguments();
            if (this.arguments.size() != litArgs.size()) {
                return false;
            }

            Map<String, String> substitution = new HashMap<>();
            for (int i = 0; i < this.arguments.size(); i++) {
                String arg1 = this.arguments.get(i);
                String arg2 = litArgs.get(i);
                if (!arg1.equals(arg2)) {
                    if (isVariable(arg1)) {
                        if (substitution.containsKey(arg1) && !substitution.get(arg1).equals(arg2)) {
                            return false;
                        }
                        substitution.put(arg1, arg2);
                    } else if (isVariable(arg2)) {
                        if (substitution.containsKey(arg2) && !substitution.get(arg2).equals(arg1)) {
                            return false;
                        }
                        substitution.put(arg2, arg1);
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isVariable(String str) {
            return str.length() == 1 && Character.isLowerCase(str.charAt(0));
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

        public String toKeyString() {
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
            literals.sort(Comparator.comparing(Literal::toString));
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

        @Override
        public int hashCode() {
            return Objects.hash(literals);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Clause other = (Clause) obj;
            return Objects.equals(this.literals, other.literals);
        }
        public void addLiteral(Literal literal) {
            if (!containsLiteral(literal)) {
                literals.add(literal);
                literals.sort(Comparator.comparing(Literal::toString));
            }
        }

        private boolean supports(Literal query) {
            for (Literal clauseLit : this.getLiterals()) {
                // Check if the literals are complementary
                if (query.isComplementary(clauseLit)) {
                    // Create a substitution for the variables in the literals
                    if (query.isUnifiable(clauseLit)) {
                        // If unification succeeds, the clause supports the negated query
                        return true;
                    }
                }
            }
            // If none of the literals in the clause support the negated query, return false
            return false;
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

        public String toKeyString() {
            StringBuilder sb = new StringBuilder();
            for (Literal lit : literals) {
                sb.append(lit.toKeyString()).append(" | ");
            }
            sb.setLength(sb.length() - 3); // remove last " | "
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
            List<Clause> clause = convertToCNF(sentence);
            cnfSentences.addAll(clause);
            for(Clause cl : clause) {
                System.out.print(cl + "  ");
            }
            System.out.println("\n");
        }
        Literal queryLiteral = getLiteralFromPredicate(query);
        List<Literal> clauseLitList = new ArrayList<>();
        clauseLitList.add(queryLiteral);
        List<Literal> negClauseLitList = new ArrayList<>();
        negClauseLitList.add(queryLiteral.getNegation());
        cnfSentences.add(new Clause(negClauseLitList));
        // Perform resolution with the query and the CNF knowledge base
        boolean result = resolution(new Clause(clauseLitList), cnfSentences);

        // Output the result
        output.print(result ? "TRUE" : "FALSE");

        sc.close();
        output.close();
    }


    /* ----------------------------------------------------------CNF CONVERTION IMPLEMENTAION ------------------------------------------------------------------------------------------- */
    public static List<Clause> convertToCNF(String sentence) {
        List<Clause> cnfClauses;
        // Step 1: Rewrite implication as disjunction of negations
        if(sentence.contains("=>")) {
            String[] implicationParts = sentence.split("=>");
            cnfClauses = getClauses(negate(implicationParts[0].trim()));

            //Distributing implication conclusion to clauses on left
            for(Clause clause : cnfClauses) {
                clause.addLiteral(getLiteralFromPredicate(implicationParts[1].trim()));
            }
        }
        else {
            cnfClauses = getClauses(convertToCNFWithoutImplication(sentence));
        }

        return cnfClauses;
    }

    private static List<Clause> getClauses(String sentence) {
        //System.out.println(sentence.trim());
        String[] preds = sentence.trim().split("&");
        List<Clause> clauses = new ArrayList<>();
        for(String pred : preds){
            String[] lits = pred.split("\\|");
            List<Literal> literals = new ArrayList<>();
            for(String lit : lits) {
                literals.add(getLiteralFromPredicate(lit.trim()));
            }
            clauses.add(new Clause(literals));
        }
        return clauses;
    }

    private static Literal getLiteralFromPredicate(String predicate) {
        // Remove any whitespace
        String cleanedPredicate = predicate.replaceAll("\\s", "");
        boolean negated = false;
        // Check if the predicate is negated
        if (cleanedPredicate.startsWith("~")) {
            negated = true;
            cleanedPredicate = cleanedPredicate.substring(1);
        }

        // Get the predicate name
        String predicateName = cleanedPredicate.substring(0, cleanedPredicate.indexOf("("));

        // Get the predicate arguments
        String argumentsString = cleanedPredicate.substring(cleanedPredicate.indexOf("(") + 1, cleanedPredicate.indexOf(")"));
        List<String> arguments = Arrays.asList(argumentsString.split(","));
        return new Literal(predicateName, arguments, negated);
    }

    private static String convertToCNFWithoutImplication(String sentence) {

        String[] conjunctions = sentence.split("\\|"); // Split into disjunctions

        List<List<String>> clauses = new ArrayList<>();
        for (String conjunction : conjunctions) {
            String[] literals = conjunction.trim().split("&"); // Split each disjunction into conjunctions

            List<String> clause = new ArrayList<>();
            for (String literal : literals) {
                clause.add(literal);
            }
            clauses.add(clause);
        }

        // Distribute disjunctions over conjunctions
        List<List<String>> distributed = distributeDisjunctionsOverConjunctions(clauses);

        // Combine conjunctions
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < distributed.size(); i++) {
            List<String> conjunction = distributed.get(i);
            sb.append(String.join(" | ", conjunction));
            if (i != distributed.size() - 1) {
                sb.append(" & ");
            }
        }

        return sb.toString();
    }

    private static List<List<String>> distributeDisjunctionsOverConjunctions(List<List<String>> clauses) {
        if (clauses.size() == 1) {
            // Base case: only one disjunction, no distribution needed
            List<List<String>> result = new ArrayList<>();
            for (String literal : clauses.get(0)) {
                result.add(Arrays.asList(literal));
            }
            return result;
        } else {
            // Recursive case: distribute first disjunction over remaining disjunctions
            List<List<String>> distributed = distributeDisjunctionsOverConjunctions(clauses.subList(1, clauses.size()));
            List<List<String>> result = new ArrayList<>();
            for (String literal : clauses.get(0)) {
                for (List<String> conjunction : distributed) {
                    List<String> newConjunction = new ArrayList<>();
                    newConjunction.add(literal);
                    newConjunction.addAll(conjunction);
                    result.add(newConjunction);
                }
            }
            return result;
        }
    }

    private static String negate(String sentence) {
        String[] parts = sentence.trim().split("\\s+");
        StringBuilder negatedString = new StringBuilder();
        for(String part : parts) {
            part = part.trim();
            if(part.startsWith("~")) {
                negatedString.append(part.substring(1));
            }
            else if(part.equals("|")) {
                negatedString.append("&");
            }
            else if(part.equals("&")) {
                negatedString.append("|");
            }
            else {
                negatedString.append("~" + part);
            }
            negatedString.append(" ");
        }
        return negatedString.toString();
    }

    /* ----------------------------------------------------------RESOLUTION IMPLEMENTAION ------------------------------------------------------------------------------------------- */
    public static boolean resolution(Clause query, List<Clause> cnfSentences) {
        Map<String, Clause> processedClauses = new HashMap<>();
        //Set<Clause> sosSubset = getSosSubset(query.getLiterals().get(0), cnfSentences);
        Set<Clause> unitClauses;
        Set<Clause> nonUnitClauses;
        boolean newClauseGenerated = true;

        unitClauses = getUnitLiterals(cnfSentences);
        nonUnitClauses = getNonUnitClauses(cnfSentences);
        while (newClauseGenerated) {
            if (unitClauses.contains(query)) {
                return true;
            }

            List<Clause> newClauses = new ArrayList<>();
            for (Clause clause1 : unitClauses) {
                for (Clause clause2 : unitClauses) {
                    System.out.println("Resolving : " + clause1 + "  " + clause2);
                    Clause resolvent = resolve(clause1, clause2);
                    System.out.println("Resolvant : " + resolvent);
                    System.out.println();
                    if (resolvent != null && !processedClauses.containsKey(resolvent.toString())) {
                        processedClauses.put(resolvent.toString(), resolvent);
                        newClauses.add(resolvent);
                        if (resolvent.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
            for (Clause clause1 : nonUnitClauses) {
                for (Clause clause2 : unitClauses) {
                    System.out.println("Resolving : " + clause1 + "  " + clause2);
                    Clause resolvent = resolve(clause1, clause2);
                    System.out.println("Resolvant : " + resolvent);
                    System.out.println();
                    if (resolvent != null && !processedClauses.containsKey(resolvent.toString())) {
                        processedClauses.put(resolvent.toString(), resolvent);
                        newClauses.add(resolvent);
                        if (resolvent.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
            for (Clause clause1 : nonUnitClauses) {
                for (Clause clause2 : nonUnitClauses) {
                    if(clause1 == clause2) continue;
                    System.out.println("Resolving : " + clause1 + "  " + clause2);
                    Clause resolvent = resolve(clause1, clause2);
                    System.out.println("Resolvant : " + resolvent);
                    System.out.println();
                    if (resolvent != null && !processedClauses.containsKey(resolvent.toString())) {
                        processedClauses.put(resolvent.toString(), resolvent);
                        newClauses.add(resolvent);
                        if (resolvent.isEmpty()) {
                            return true;
                        }
                    }
                }
            }

            if (newClauses.isEmpty()) {
                newClauseGenerated = false;
            } else {
                for(Clause newClause : newClauses) {
                    if(newClause.getLiterals().size() == 1) {
                        unitClauses.add(newClause);
                    }
                    else {
                        nonUnitClauses.add(newClause);
                    }
                }
            }
        }

        return false;
    }

    private static Set<Clause> getNonUnitClauses(List<Clause> cnfSentences) {
        Set<Clause> nonUnitLiterals = new HashSet<>();
        for(Clause cl : cnfSentences) {
            if(cl.getLiterals().size() > 1) {
                nonUnitLiterals.add(cl);
            }
        }
        return nonUnitLiterals;
    }


    /*Clause resolvent = resolve(clause1, unitLiterals);
                if (resolvent != null && !processedClauses.contains(resolvent)) {
                    processedClauses.add(resolvent);
                    cnfSentences.add(resolvent);
                    newClauseGenerated = true;
                    if (resolvent.isEmpty()) {
                        return true;
                    }
                }*/
    private static Clause resolve(Clause c1, Clause c2) {
        // Find complementary literals
        List<Literal> l1 = new ArrayList<>(c1.getLiterals());
        List<Literal> l2 = new ArrayList<>(c2.getLiterals());
        List<Literal> resolvedLiterals = new ArrayList<>();
        for (Literal lit1 : l1) {
            for (Literal lit2 : l2) {
                if (lit1.isComplementary(lit2) && lit1.isUnifiable(lit2)) {
                    // Unify the literals and add them to the resolved literals list
                    Map<String, String> unifier = unify(lit1, lit2);
                    for (Literal l : l1) {
                        if (!l.equals(lit1)) {
                            resolvedLiterals.add(substitute(l, unifier));
                        }
                    }
                    for (Literal l : l2) {
                        if (!l.equals(lit2)) {
                            resolvedLiterals.add(substitute(l, unifier));
                        }
                    }
                    return new Clause(resolvedLiterals);
                }
            }
        }

        return null;
    }

    private static Map<String, String> unify(Literal lit1, Literal lit2) {
        Map<String, String> unifier = new HashMap<>();
        List<String> args1 = lit1.getArguments();
        List<String> args2 = lit2.getArguments();
        for(int i=0; i< args1.size(); i++) {
            if(!isVariable(args1.get(i))) {
                unifier.put(args2.get(i), args1.get(i));
            }
            else {
                unifier.put(args1.get(i), args2.get(i));
            }
        }
        return unifier;
    }

    private static boolean isVariable(String str) {
        return str.length() == 1 && Character.isLowerCase(str.charAt(0));
    }
    private static Literal substitute(Literal l, Map<String, String> unifier) {
        List<String> newArguments = new ArrayList<>();
        List<String> arguments = l.getArguments();
        for(String arg : arguments) {
            newArguments.add(unifier.getOrDefault(arg, arg));
        }
        return new Literal(l.getPredicate(), newArguments, l.isNegated());
    }

    private static Set<Clause> getUnitLiterals(List<Clause> cnfSentences) {
        Set<Clause> unitLiterals = new HashSet<>();
        for(Clause cl : cnfSentences) {
            if(cl.getLiterals().size() == 1) {
                unitLiterals.add(cl);
            }
        }
        return unitLiterals;
    }

    private static Set<Clause> getSosSubset(Literal query, List<Clause> cnfSentences) {
        Set<Clause> sosSubset = new HashSet<>();
        List<Clause> unprocessed = new ArrayList<>(cnfSentences);
        // Loop until there are no more unprocessed clauses
        while (!unprocessed.isEmpty()) {
            // Get the next unprocessed clause
            Clause clause = unprocessed.remove(0);

            // Check if the clause supports the query
            if (clause.supports(query)) {
                // Add the clause to the Set-of-Support subset
                sosSubset.add(clause);

                // Remove any clauses that are no longer needed from the unprocessed list
                unprocessed.removeAll(sosSubset);

                // If there are no more unprocessed clauses that can support the query, exit the loop
                if (unprocessed.stream().noneMatch(c -> c.supports(query))) {
                    break;
                }
            }
        }

        // Return the Set-of-Support subset
        return sosSubset;
    }


}
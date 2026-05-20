import es.ulpgc.dacd.business.util.TechnologyNormalizer;

public class Main {
    public static void main(String[] args) {
        // Lista de casos para verificar visualmente
        String[] tests = {"js", "node.js", "C#", "python3", "unknownTech"};

        System.out.println("--- Probando TechnologyNormalizer ---");
        for (String input : tests) {
            String result = TechnologyNormalizer.normalize(input);
            System.out.println("Entrada: '" + input + "' -> Normalizado: '" + result + "'");
        }
    }
}
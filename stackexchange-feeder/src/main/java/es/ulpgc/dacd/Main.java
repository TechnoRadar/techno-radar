package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.StackExchangeClient;
import es.ulpgc.dacd.persistence.SqliteDatabaseManager;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2){
            System.out.println("Error: No se ha proporcionado la URL de la fuente externa.");
            System.out.println("Uso: java Main <stackexchangeUrl> <dbPath>");
            return;
        }

        String stackexchangeUrl = args[0];
        String dbPath = args[1];

        System.out.println("Iniciando Techno-Radar (Módulo StackExchange)...");
        System.out.println("URL configurada: " + stackexchangeUrl);
        System.out.println("Base de Datos: " + dbPath);

        SqliteDatabaseManager store = new SqliteDatabaseManager(dbPath);
        StackExchangeClient feeder = new StackExchangeClient(stackexchangeUrl);

        Controller controller = new Controller(feeder, store);
        controller.execute();
    }
}
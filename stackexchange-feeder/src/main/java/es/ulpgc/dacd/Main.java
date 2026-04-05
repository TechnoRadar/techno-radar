package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.StackExchangeClient;
import es.ulpgc.dacd.persistence.SqliteDatabaseManager;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Techno-Radar (Módulo StackExchange)...");
        SqliteDatabaseManager store = new SqliteDatabaseManager("stackexchange_trends.db");

        StackExchangeClient feeder = new StackExchangeClient();
        Controller controller = new Controller(feeder, store);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Descargando datos de StackExchange y guardando en SQLite...");
                controller.execute();
                System.out.println("Actualización completada con éxito.");
            }
        }, 0, 3600000);
    }
}
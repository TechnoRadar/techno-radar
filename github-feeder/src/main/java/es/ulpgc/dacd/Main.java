package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.GitHubClient;
import es.ulpgc.dacd.persistence.SqliteDatabaseManager;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Techno-Radar (Módulo GitHub)...");
        SqliteDatabaseManager store = new SqliteDatabaseManager("github_trends.db");

        GitHubClient feeder = new GitHubClient();
        Controller controller = new Controller(feeder, store);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Descargando datos de GitHub y guardando en SQLite...");
                controller.execute();
                System.out.println("Actualización completada con éxito.");
            }
        }, 0, 3600000);
    }
}
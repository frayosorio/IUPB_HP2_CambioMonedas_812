package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import entidades.CambioMoneda;

public class CambioMonedaServicio {

    public static List<CambioMoneda> getDatos(String nombreArchivo) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            Stream<String> lineas = Files.lines(Paths.get(nombreArchivo));

            return lineas.skip(1)
                    .map(linea -> linea.split(","))
                    .map(textos -> new CambioMoneda(textos[0],
                            LocalDate.parse(textos[1], formatoFecha),
                            Double.parseDouble(textos[2])))
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public static List<String> getMonedas(List<CambioMoneda> cambiosMonedas) {
        return cambiosMonedas.stream()
                .map(CambioMoneda::getMoneda)// .map(item -> item.getMoneda())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<CambioMoneda> filtrar(List<CambioMoneda> cambioMonedas,
            String moneda, LocalDate desde, LocalDate hasta) {
        return cambioMonedas.stream()
                .filter(item -> item.getMoneda().equals(moneda) &&
                        !(item.getFecha().isBefore(desde) ||
                                item.getFecha().isAfter(hasta)))
                .collect(Collectors.toList());
    }

    public static Map<LocalDate, Double> extraerDatosGrafica(List<CambioMoneda> cambios) {
        return cambios.stream()
                .collect(Collectors.toMap(CambioMoneda::getFecha, CambioMoneda::getCambio));
    }

    public static double getPromedio(List<Double> datos) {
        return datos.isEmpty() ? 0
                : datos.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0);
    }

    public static double getDesviacionEstandar(List<Double> datos) {
        var promedio = getPromedio(datos);
        return datos.isEmpty() ? 0
                : Math.sqrt(
                        datos.stream()
                                .mapToDouble(dato -> Math.pow(dato - promedio, 2))
                                .average()
                                .orElse(0));
    }

    public static double getMaximo(List<Double> datos) {
        return datos.isEmpty() ? 0
                : datos.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0);
    }

    public static double getMinimo(List<Double> datos) {
        return datos.isEmpty() ? 0
                : datos.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(0);
    }

    public static double getMediana(List<Double> datos) {
        if (datos.isEmpty()) {
            return 0;
        }
        var datosOrdenados = datos.stream().sorted().collect(Collectors.toList());
        var n = datosOrdenados.size();
        return n % 2 == 0 ? (datosOrdenados.get(n / 2) + datosOrdenados.get(n / 2 - 1)) / 2 : datosOrdenados.get(n / 2);
    }

    public static double getModa(List<Double> datos) {
        if (datos.isEmpty()) {
            return 0;
        }

        return datos.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0.0);
    }

    public static Map<String, Double> getEstadisticas(List<CambioMoneda> cambioMonedas,
            String moneda, LocalDate desde, LocalDate hasta) {

        var datosFiltrados = filtrar(cambioMonedas, moneda, desde, hasta);
        var cambios = datosFiltrados.stream()
                .map(CambioMoneda::getCambio)
                .collect(Collectors.toList());

        Map<String, Double> estadisticas = new LinkedHashMap<>();
        estadisticas.put("Promedio", getPromedio(cambios));
        estadisticas.put("Desviación Estandar", getDesviacionEstandar(cambios));
        estadisticas.put("Máximo", getMaximo(cambios));
        estadisticas.put("Mínimo", getMinimo(cambios));
        estadisticas.put("Mediana", getMediana(cambios));
        estadisticas.put("Moda", getModa(cambios));

        return estadisticas;
    }

}

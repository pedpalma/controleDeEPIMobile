package br.senai.sp.controleepi.util;

import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class Datas {

    public static final int DIAS_ALERTA_CA = 30;

    private static final ZoneId ZONA = ZoneId.systemDefault();
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter BR_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private Datas() {
    }

    public static LocalDate paraLocalDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toDate().toInstant().atZone(ZONA).toLocalDate();
    }

    public static Timestamp paraTimestamp(LocalDate data) {
        return new Timestamp(Date.from(data.atStartOfDay(ZONA).toInstant()));
    }

    public static String formatar(LocalDate data) {
        return data == null ? "" : data.format(BR);
    }

    public static String formatar(Timestamp timestamp) {
        return formatar(paraLocalDate(timestamp));
    }

    public static String formatarComHora(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return timestamp.toDate().toInstant().atZone(ZONA).format(BR_HORA);
    }

    public static String paraTexto(LocalDate data) {
        return data == null ? null : data.toString();
    }

    public static LocalDate deTexto(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static long diasAte(Timestamp timestamp) {
        LocalDate data = paraLocalDate(timestamp);
        if (data == null) {
            return Long.MIN_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), data);
    }

    public static boolean vencido(Timestamp validade) {
        return validade != null && diasAte(validade) < 0;
    }

    public static Timestamp limiteAlertaCa() {
        return paraTimestamp(LocalDate.now().plusDays(DIAS_ALERTA_CA + 1));
    }

    public static String situacaoCa(Timestamp validade) {
        if (validade == null) {
            return "Sem validade";
        }
        long dias = diasAte(validade);
        if (dias < 0) {
            long passados = -dias;
            return passados == 1 ? "Vencido há 1 dia" : "Vencido há " + passados + " dias";
        }
        if (dias == 0) {
            return "Vence hoje";
        }
        if (dias <= DIAS_ALERTA_CA) {
            return dias == 1 ? "Vence amanhã" : "Vence em " + dias + " dias";
        }
        return "CA válido";
    }
}

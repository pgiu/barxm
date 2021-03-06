/*
 * Copyright (C) 2015 pgiudice
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.pgiu.barx;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author pgiudice
 */
public class GaliciaParser {

    private Document doc;
    private NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
    private SimpleDateFormat standardDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private Map<String, String> map;

    /**
     * Constructor sin mapa de consumos.
     *
     * @param doc es el documento a leer
     */
    GaliciaParser(Document doc) {
        this.doc = doc;
    }

    /**
     * Constructor con mapa de consumos.
     *
     * @param doc es el documento a leer
     * @param map cuyas claves son regexs y los valores son la cuenta a la cual
     * se asigna el consumo.
     */
    GaliciaParser(Document doc, Map<String, String> map) {
        this.doc = doc;
        this.map = map;
    }

    /**
     * Devuelve los consumos de un resumen.
     *
     */
    List<Consumo> getConsumos() {

        List<Consumo> c = new ArrayList();

        Element table = doc.select("table.noBorderTop[width=590]").first();
        Iterator<Element> row = table.select("tr").iterator();

        //
        AccountMatcher am = new AccountMatcher(map);

        while (row.hasNext()) {

            Iterator<Element> td = row.next().select("td").iterator();
            String fecha = td.next().text();//fecha
            String movimiento = td.next().text();
            String debitoAsString = td.next().text();//debito
            String creditoAsString = td.next().text();//credito
            String saldoAsText = td.next().text();//saldo
            String detalle = td.next().text();//detalle

            // Debug only
            System.out.println("Fecha:" + fecha + "Lugar:\t" + movimiento + "\tSaldo:" + saldoAsText); //

            // Intento de que tipo de gasto se trata
            String cuenta = am.findMatch(movimiento);

            // Parseo de fechas y números
            try {
                //Parseo la fecha 
                Date date = standardDateFormat.parse(fecha);

                //Parseo el debito
                Number debitoAsNumber = format.parse(debitoAsString);
                double debitoAsDouble = debitoAsNumber.doubleValue();

                //Parseo el credito
                Number creditoAsNumber = format.parse(creditoAsString);
                double creditoAsDouble = creditoAsNumber.doubleValue();

                //Parseo el saldo
                Number saldo = format.parse(saldoAsText);
                double saldoAsDouble = saldo.doubleValue();

                Consumo consumo = new Consumo(date, movimiento, debitoAsDouble, creditoAsDouble, saldoAsDouble, detalle);
                consumo.setCuenta(cuenta);
                c.add(consumo);

            } catch (ParseException ex) {
                Logger.getLogger(GaliciaParser.class.getName()).log(Level.SEVERE, null, ex);
                //throw new ParseException("a",1); TODO define better exception handling
            }

        }

        return c;
    }

    /**
     * Devuelve el saldo de un resumen.
     * 
     */
    void getSaldo(List<Date> t, List<Double> saldo) throws IOException, ParseException {

        List<Consumo> con = getConsumos();
        for (Consumo c : con) {
            t.add(c.fecha);
            saldo.add(c.saldoParcial);
        }
    }
}

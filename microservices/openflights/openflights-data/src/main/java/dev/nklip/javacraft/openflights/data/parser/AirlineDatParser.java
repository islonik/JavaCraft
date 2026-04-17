package dev.nklip.javacraft.openflights.data.parser;

import dev.nklip.javacraft.openflights.api.Airline;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class AirlineDatParser {

    public Airline parseLine(String line) throws IOException {
        return Airline.fromColumns(OpenFlightsCsvSupport.parseLine(line));
    }

    public List<Airline> parseStream(InputStream inputStream) throws IOException {
        return OpenFlightsCsvSupport.parseStream(inputStream, Airline::fromColumns);
    }
}

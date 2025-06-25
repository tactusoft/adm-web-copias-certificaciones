package co.gov.sic.copiasycertificaciones.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import sic.ws.interop.entities.radicacion.Radicacion;

public class Functions {

    public static String saveFile(Radicacion radi, ByteArrayOutputStream fileContent, String inpuFileName) throws Exception {
        String num_radi = "";
        if (String.valueOf(radi.getNumero()).length() < 5) {
            num_radi = String.valueOf(radi.getAnio()) + "-00" + String.valueOf(radi.getNumero());
        } else if (String.valueOf(radi.getNumero()).length() < 6) {
            num_radi = String.valueOf(radi.getAnio()) + "-0" + String.valueOf(radi.getNumero());
        }

        String fileName = String.format("%s_%s", num_radi, inpuFileName);
        return saveFile(radi.getAnio(), radi.getNumero(), fileName, fileContent);
    }

    public static String saveFile(Radicacion radi, ByteArrayOutputStream fileContent) throws Exception {
        String fileName = getRadicacionileName(radi);
        return saveFile(radi.getAnio(), radi.getNumero(), fileName, fileContent);
    }

    public static String getRadicacionileName(Radicacion radi) {
        return String.format("%s.%s", radi.getFullNumeroRadicacion(), Constantes.PDF_EXTENSION);
    }

    public static Path getRadicacionFullPathFileName(Radicacion radi) throws Exception {
        String fileName = getRadicacionileName(radi);
        String directory = getRadicacionFolderPath(radi);
        Path targetLocation = Paths.get(directory);
        targetLocation = targetLocation.resolve(fileName);
        return targetLocation;
    }

    public static String saveFile(Radicacion radi, String fileName, ByteArrayOutputStream fileContent) throws Exception {
        return saveFile(radi.getAnio(), radi.getNumero(), fileName, fileContent);
    }

    public static String saveFile(Radicacion radi, String fileName, byte[] fileBytes) throws Exception {
        if (fileBytes != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(fileBytes.length)) {
                baos.write(fileBytes, 0, fileBytes.length);
                return saveFile(radi.getAnio(), radi.getNumero(), fileName, baos);
            }
        }
        return null;
    }

    public static String getRadicacionFolderPath(Radicacion radi) {
        return getRadicacionFolderPath(radi.getAnio(), radi.getNumero());
    }

    public static String getRadicacionFolderPath(short ano_radi, int nume_radi) {
        String directory = String.format("%s%02d/%02d-%06d/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, ano_radi, ano_radi, nume_radi);
        return directory;
    }
    
     public static String getAdjuntosFolderPath(long idenPers) {
        String directory = String.format("%s/temp/%s/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, idenPers);
        return directory;
    }

    public static String saveFile(short ano_radi, int nume_radi, String fileName, ByteArrayOutputStream fileContent) throws Exception {
        String directory = getRadicacionFolderPath(ano_radi, nume_radi);
        Path targetLocation = Paths.get(directory);
        if (!Files.exists(targetLocation)) {
            Files.createDirectories(targetLocation);
        }
        targetLocation = targetLocation.resolve(fileName);
        directory = targetLocation.toString();
        try (OutputStream outputStream = new FileOutputStream(directory)) {
            fileContent.writeTo(outputStream);
        }
        return directory;
    }
}

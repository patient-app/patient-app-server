package ch.uzh.ifi.imrg.patientapp.utils;

import ch.uzh.ifi.imrg.patientapp.entity.Document.PatientDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.jsoup.Jsoup;

/**
 * DocumentUtil uses a strategy pattern to extract text from various document
 * types.
 */
public class DocumentUtil {

    private static final List<TextExtractor> EXTRACTORS = Arrays.asList(
            new PdfExtractor(),
            new DocxExtractor(),
            new DocExtractor(),
            new PlainTextExtractor(),
            new HtmlExtractor());

    /**
     * Extracts text using the first matching extractor, or returns Base64 fallback.
     */
    public static ExtractionResult extractTextResult(PatientDocument pd) {
        byte[] data = pd.getDocument().getData();
        String filename = pd.getDocument().getFilename().toLowerCase();
        String contentType = pd.getDocument().getContentType();

        for (TextExtractor extractor : EXTRACTORS) {
            if (extractor.supports(contentType, filename)) {
                try {
                    Optional<String> text = extractor.extract(data);
                    if (text.isPresent() && !text.get().isBlank()) {
                        return new ExtractionResult(text.get(), true);
                    }
                } catch (IOException ignored) {
                }
                break; // matched but no text: fall back
            }
        }
        // fallback
        String encoded = Base64.getEncoder().encodeToString(data);
        return new ExtractionResult(encoded, false);
    }

    public static String extractText(PatientDocument pd) {
        return extractTextResult(pd).getText();
    }

    public static boolean isTextExtractable(PatientDocument pd) {
        return extractTextResult(pd).isHumanReadable();
    }

    /** Holds result data. */
    public static class ExtractionResult {
        private final String text;
        private final boolean humanReadable;

        public ExtractionResult(String text, boolean humanReadable) {
            this.text = text;
            this.humanReadable = humanReadable;
        }

        public String getText() {
            return text;
        }

        public boolean isHumanReadable() {
            return humanReadable;
        }
    }

    /** Strategy interface. */
    private interface TextExtractor {
        boolean supports(String contentType, String filename);

        Optional<String> extract(byte[] data) throws IOException;
    }

    private static class PdfExtractor implements TextExtractor {
        public boolean supports(String c, String f) {
            return (c != null && c.equals("application/pdf")) || f.endsWith(".pdf");
        }

        public Optional<String> extract(byte[] data) throws IOException {
            try (PDDocument pdf = PDDocument.load(new ByteArrayInputStream(data))) {
                return Optional.of(new PDFTextStripper().getText(pdf));
            }
        }
    }

    private static class DocxExtractor implements TextExtractor {
        public boolean supports(String c, String f) {
            return (c != null && c.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    || f.endsWith(".docx");
        }

        public Optional<String> extract(byte[] data) throws IOException {
            try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(data))) {
                return Optional.of(new XWPFWordExtractor(docx).getText());
            }
        }
    }

    private static class DocExtractor implements TextExtractor {
        public boolean supports(String c, String f) {
            return (c != null && c.equals("application/msword")) || f.endsWith(".doc");
        }

        public Optional<String> extract(byte[] data) throws IOException {
            try (HWPFDocument doc = new HWPFDocument(new ByteArrayInputStream(data))) {
                return Optional.of(new WordExtractor(doc).getText());
            }
        }
    }

    private static class PlainTextExtractor implements TextExtractor {
        public boolean supports(String c, String f) {
            return (c != null && c.startsWith("text/plain")) || f.endsWith(".txt");
        }

        public Optional<String> extract(byte[] data) {
            return Optional.of(new String(data, StandardCharsets.UTF_8));
        }
    }

    private static class HtmlExtractor implements TextExtractor {
        public boolean supports(String c, String f) {
            return (c != null && c.startsWith("text/html")) || f.endsWith(".html") || f.endsWith(".htm");
        }

        public Optional<String> extract(byte[] data) {
            String html = new String(data, StandardCharsets.UTF_8);
            return Optional.of(Jsoup.parse(html).text());
        }
    }
}

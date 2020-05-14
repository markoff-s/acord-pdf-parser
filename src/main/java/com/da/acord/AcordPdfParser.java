package com.da.acord;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.*;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AcordPdfParser {
    public static void main(@org.jetbrains.annotations.NotNull String[] args) throws IOException {
        System.out.println("Starting app...");
        String pathToPdfForm = args[0];
        System.out.println(String.format("Path to pdf form is '%s'", pathToPdfForm));

        File pdfFile = new File(pathToPdfForm);
        if (!pdfFile.exists())
            throw new FileNotFoundException();

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfFile.getAbsolutePath()));
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        List<String[]> resultsList = new ArrayList<String[]>();
        resultsList.add(new String[]{"ACORD field name", "Question", "Type"});
        for (Map.Entry<String, PdfFormField> entry: form.getFormFields().entrySet()) {
            String formFieldName = entry.getKey();
            PdfFormField formField = entry.getValue();
            String formFieldDescription = getFormFieldDescription(formField);
            String formFieldType = getFormFieldType(formField);

            System.out.println(formFieldName + " ===> " + formFieldDescription + " ===> " + formFieldType);

            resultsList.add(new String[]{formFieldName, formFieldDescription, formFieldType});
        }

        if (args.length > 1 && resultsList.size() > 0) {
            String pathToCsvFile = args[1];
            CSVWriter writer = new CSVWriter(new FileWriter(pathToCsvFile));
            writer.writeAll(resultsList);
            writer.close();
        }
    }

    private static String getFormFieldDescription(PdfFormField formField) {
        // e.g. "Enter text: The mailing address line one of the producer / agency."
        String fieldDesc = getFormFieldFullDescription(formField);

        // drop text before the ':' char
        int splitIndex = fieldDesc.indexOf(':');
        if (splitIndex > -1)
            return fieldDesc.split(":", 2)[1];
        else
            return fieldDesc;
    }

    private static String getFormFieldType(PdfFormField formField) {
        // e.g. "Enter text: The mailing address line one of the producer / agency."
        String fieldDesc = getFormFieldFullDescription(formField);

        // string
        String stringTypeRegex = "[\\w\\s]*(text)[\\w\\s]*:";
        if (IsType(fieldDesc, stringTypeRegex)) return "STRING";

        String numberTypeRegex = "[\\w\\s]*((\\bnumber\\b)|(\\bamount\\b)|(\\blimit\\b)|(\\brate\\b)|(\\byear\\b))[\\w\\s]*:";
        if (IsType(fieldDesc, numberTypeRegex)) return "NUMBER";

        String dateTypeRegex = "[\\w\\s]*(date)[\\w\\s]*:";
        if (IsType(fieldDesc, dateTypeRegex)) return "DATE";

        String codeTypeRegex = "[\\w\\s]*(code)[\\w\\s]*:";
        if (IsType(fieldDesc, codeTypeRegex)) return "CODE";

        String identifierTypeRegex = "[\\w\\s]*(identifier)[\\w\\s]*:";
        if (IsType(fieldDesc, identifierTypeRegex)) return "IDENTIFIER";

        String percentageTypeRegex = "[\\w\\s]*(percentage)[\\w\\s]*:";
        if (IsType(fieldDesc, percentageTypeRegex)) return "PERCENTAGE";

        String checkboxTypeRegex = "(Check\\s+the\\s+box)[\\w\\s\\(\\)]*:";
        if (IsType(fieldDesc, checkboxTypeRegex)) return "CHECKBOX";

        return String.format("NO MATCH (%s)", fieldDesc);
    }

    private static boolean IsType(String fieldDesc, String stringTypeRegex) {
        Pattern stringTypePattern = Pattern.compile(stringTypeRegex);
        Matcher stringMather = stringTypePattern.matcher(fieldDesc);
        if (stringMather.find( )) {
            return true;
        }
        return false;
    }

    private static String getFormFieldFullDescription(PdfFormField formField) {
        final String descriptionKeyName = "TU";
        PdfName key = new PdfName(descriptionKeyName);
        PdfDictionary pdfObject = formField.getPdfObject();
        String fieldDesc = "";
        if (pdfObject.containsKey(key)) {
            fieldDesc = pdfObject.getAsString(key).getValue();
        }
        return fieldDesc;
    }
}

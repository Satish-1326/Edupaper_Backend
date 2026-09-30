package com.edupaper.service;

import com.edupaper.dto.document.DocumentProcessingResponse;
import com.edupaper.dto.document.UnitContentResponse;
import com.edupaper.entity.DocumentStatus;
import com.edupaper.entity.DocumentUnitContent;
import com.edupaper.entity.Unit;
import com.edupaper.entity.UploadedDocument;
import com.edupaper.exception.BadRequestException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.DocumentUnitContentRepository;
import com.edupaper.repository.UnitRepository;
import com.edupaper.repository.UploadedDocumentRepository;
import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PdfProcessingService {

    private final UploadedDocumentRepository documentRepository;

    private final DocumentUnitContentRepository
            documentUnitContentRepository;

    private final UnitRepository unitRepository;


    /*
     * Supports headings such as:
     *
     * UNIT 1
     * UNIT 1:
     * UNIT 1 - Introduction
     * UNIT I
     * UNIT I: Introduction
     * Unit 1
     * MODULE 1
     */
    private static final Pattern UNIT_PATTERN =
            Pattern.compile(
                    "(?im)^\\s*" +
                            "\\[\\s*(?:UNIT|MODULE)\\s*" +
                            "(I{1,3}|IV|V|1|2|3|4|5)" +
                            "\\s*\\]" +
                            "\\s*" +
                            "([^\\r\\n]*)"
            );


    @Transactional
    public DocumentProcessingResponse processDocument(
            Long documentId,
            Long userId
    ) {

        UploadedDocument document =
                documentRepository
                        .findByIdAndUploadedById(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"
                                )
                        );

        if (document.getStatus() ==
                DocumentStatus.PROCESSING) {

            throw new BadRequestException(
                    "Document is already being processed."
            );
        }

        Path pdfPath =
                Paths.get(
                        document.getFilePath()
                );

        if (!Files.exists(pdfPath)) {

            document.setStatus(
                    DocumentStatus.FAILED
            );

            documentRepository.save(document);

            throw new BadRequestException(
                    "PDF file was not found on the server."
            );
        }

        try {

            document.setStatus(
                    DocumentStatus.PROCESSING
            );

            documentRepository.save(document);


            // ------------------------------------
            // 1. Extract complete PDF text
            // ------------------------------------

            String extractedText;

            try (
                    PDDocument pdf =
                            Loader.loadPDF(
                                    pdfPath.toFile()
                            )
            ) {

                PDFTextStripper stripper =
                        new PDFTextStripper();

                extractedText =
                        stripper.getText(pdf);

                System.out.println(
                        "\n========== EXTRACTED PDF TEXT ==========\n"
                );

                System.out.println(extractedText);

                System.out.println(
                        "\n========== END EXTRACTED TEXT ==========\n"
                );
            }


            if (extractedText == null ||
                    extractedText.isBlank()) {

                throw new BadRequestException(
                        "No readable text was found in the PDF."
                );
            }


            // ------------------------------------
            // 2. Detect units
            // ------------------------------------

            Map<Integer, String> unitContents =
                    splitIntoUnits(
                            extractedText
                    );


            // ------------------------------------
            // 3. Require all 5 units
            // ------------------------------------

            for (int unitNumber = 1;
                 unitNumber <= 5;
                 unitNumber++) {

                if (!unitContents.containsKey(
                        unitNumber
                )) {

                    throw new BadRequestException(
                            "Could not detect Unit "
                                    + unitNumber
                                    + " in the PDF."
                    );
                }
            }


            // ------------------------------------
            // 4. Get database units
            // ------------------------------------

            List<Unit> databaseUnits =
                    unitRepository
                            .findBySubjectIdOrderByUnitNumberAsc(
                                    document
                                            .getSubject()
                                            .getId()
                            );


            Map<Integer, Unit> unitsByNumber =
                    new HashMap<>();

            for (Unit unit : databaseUnits) {

                unitsByNumber.put(
                        unit.getUnitNumber(),
                        unit
                );
            }


            // ------------------------------------
            // 5. Make sure DB has Units 1-5
            // ------------------------------------

            for (int unitNumber = 1;
                 unitNumber <= 5;
                 unitNumber++) {

                if (!unitsByNumber.containsKey(
                        unitNumber
                )) {

                    throw new BadRequestException(
                            "Database does not contain Unit "
                                    + unitNumber
                    );
                }
            }


            // ------------------------------------
            // 6. Remove previous processing
            // ------------------------------------

            documentUnitContentRepository
                    .deleteByDocumentId(
                            documentId
                    );


            // ------------------------------------
            // 7. Save each unit's content
            // ------------------------------------

            List<UnitContentResponse> responses =
                    new ArrayList<>();

            int totalCharacters = 0;

            for (int unitNumber = 1;
                 unitNumber <= 5;
                 unitNumber++) {

                Unit unit =
                        unitsByNumber.get(
                                unitNumber
                        );

                String content =
                        cleanText(
                                unitContents.get(
                                        unitNumber
                                )
                        );

                if (content.isBlank()) {

                    throw new BadRequestException(
                            "Unit "
                                    + unitNumber
                                    + " contains no readable content."
                    );
                }

                int characterCount =
                        content.length();

                totalCharacters +=
                        characterCount;


                DocumentUnitContent
                        unitContent =
                        DocumentUnitContent
                                .builder()
                                .document(document)
                                .unit(unit)
                                .content(content)
                                .characterCount(
                                        characterCount
                                )
                                .build();

                documentUnitContentRepository
                        .save(unitContent);


                responses.add(
                        UnitContentResponse
                                .builder()
                                .unitId(
                                        unit.getId()
                                )
                                .unitNumber(
                                        unit.getUnitNumber()
                                )
                                .unitTitle(
                                        unit.getTitle()
                                )
                                .characterCount(
                                        characterCount
                                )
                                .content(content)
                                .build()
                );
            }


            // ------------------------------------
            // 8. Mark processed
            // ------------------------------------

            document.setStatus(
                    DocumentStatus.PROCESSED
            );

            documentRepository.save(document);


            return DocumentProcessingResponse
                    .builder()
                    .documentId(
                            document.getId()
                    )
                    .fileName(
                            document.getFileName()
                    )
                    .status(
                            DocumentStatus.PROCESSED
                    )
                    .totalUnitsDetected(5)
                    .totalCharacters(
                            totalCharacters
                    )
                    .units(responses)
                    .build();


        } catch (BadRequestException e) {

            document.setStatus(
                    DocumentStatus.FAILED
            );

            documentRepository.save(document);

            throw e;

        } catch (Exception e) {

            document.setStatus(
                    DocumentStatus.FAILED
            );

            documentRepository.save(document);

            throw new BadRequestException(
                    "Failed to process PDF: "
                            + e.getMessage()
            );
        }
    }


    /*
     * Split complete PDF text into:
     *
     * Unit 1 → content
     * Unit 2 → content
     * ...
     * Unit 5 → content
     */
    private Map<Integer, String> splitIntoUnits(
            String text
    ) {

        Matcher matcher =
                UNIT_PATTERN.matcher(text);

        List<UnitMarker> markers =
                new ArrayList<>();

        while (matcher.find()) {

            String unitIdentifier =
                    matcher.group(1);

            Integer unitNumber =
                    parseUnitNumber(
                            unitIdentifier
                    );

            if (unitNumber == null ||
                    unitNumber < 1 ||
                    unitNumber > 5) {

                continue;
            }

            markers.add(
                    new UnitMarker(
                            unitNumber,
                            matcher.start(),
                            matcher.end()
                    )
            );
        }


        if (markers.isEmpty()) {

            throw new BadRequestException(
                    "No unit headings were detected. " +
                            "Expected headings such as UNIT 1, " +
                            "UNIT I, UNIT 2, etc."
            );
        }


        Map<Integer, String> result =
                new LinkedHashMap<>();


        for (int i = 0;
             i < markers.size();
             i++) {

            UnitMarker current =
                    markers.get(i);

            int contentStart =
                    current.end;

            int contentEnd =
                    i + 1 < markers.size()
                            ? markers
                            .get(i + 1)
                            .start
                            : text.length();

            String content =
                    text.substring(
                            contentStart,
                            contentEnd
                    );

            /*
             * If a heading appears more than once,
             * combine its content rather than silently
             * overwriting it.
             */
            if (result.containsKey(
                    current.unitNumber
            )) {

                String existing =
                        result.get(
                                current.unitNumber
                        );

                result.put(
                        current.unitNumber,
                        existing
                                + "\n"
                                + content
                );

            } else {

                result.put(
                        current.unitNumber,
                        content
                );
            }
        }

        return result;
    }


    private Integer parseUnitNumber(
            String value
    ) {

        String normalized =
                value
                        .trim()
                        .toUpperCase();

        switch (normalized) {

            case "I":
                return 1;

            case "II":
                return 2;

            case "III":
                return 3;

            case "IV":
                return 4;

            case "V":
                return 5;

            case "1":
                return 1;

            case "2":
                return 2;

            case "3":
                return 3;

            case "4":
                return 4;

            case "5":
                return 5;

            default:
                return null;
        }
    }


    private String cleanText(String text) {

        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\u000C", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }


    private static class UnitMarker {

        private final Integer unitNumber;

        private final int start;

        private final int end;

        private UnitMarker(
                Integer unitNumber,
                int start,
                int end
        ) {

            this.unitNumber =
                    unitNumber;

            this.start = start;

            this.end = end;
        }
    }
}
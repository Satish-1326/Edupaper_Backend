package com.edupaper.service;

import com.edupaper.entity.Paper;
import com.edupaper.entity.PaperQuestion;
import com.edupaper.exception.BadRequestException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.PaperRepository;

import lombok.RequiredArgsConstructor;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final PaperRepository paperRepository;

    // =====================================================
    // GENERATE QUESTION PAPER PDF
    // =====================================================

    public byte[] generateQuestionPaperPdf(
            Long paperId,
            Long userId) {

        // =================================================
        // FIND PAPER
        // =================================================

        Paper paper =
                paperRepository
                        .findByIdAndCreatedById(
                                paperId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Paper not found"
                                )
                        );

        // =================================================
        // ONLY FINALIZED PAPERS
        // =================================================

        if (paper.getStatus() == null ||
                !paper.getStatus()
                        .name()
                        .equals("FINALIZED")) {

            throw new BadRequestException(
                    "Only finalized papers can be exported as PDF"
            );
        }

        // =================================================
        // VALIDATE PAPER
        // =================================================

        if (paper.getQuestions() == null ||
                paper.getQuestions().isEmpty()) {

            throw new BadRequestException(
                    "Paper does not contain any questions"
            );
        }

        // =================================================
        // CREATE PDF
        // =================================================

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(
                            org.openpdf.text.PageSize.A4,
                            50,
                            50,
                            50,
                            50
                    );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            // =================================================
            // FONTS
            // =================================================

            Font collegeFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            16
                    );

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            14
                    );

            Font normalFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            11
                    );

            Font questionFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            11
                    );

            // =================================================
            // COLLEGE HEADER
            // =================================================

            Paragraph college =
                    new Paragraph(
                            "Karmveer Bhaurao Patil College of Engineering",
                            collegeFont
                    );

            college.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(college);

            // =================================================
            // SUBJECT
            // =================================================

            String subjectName =
                    paper.getSubject() != null
                            ? paper.getSubject().getName()
                            : "Subject";

            Paragraph subject =
                    new Paragraph(
                            subjectName,
                            titleFont
                    );

            subject.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(subject);

            // =================================================
            // EXAM TITLE
            // =================================================

            Paragraph examTitle =
                    new Paragraph(
                            paper.getName(),
                            titleFont
                    );

            examTitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(examTitle);

            // =================================================
            // SUBJECT DETAILS
            // =================================================

            if (paper.getSubject() != null) {

                StringBuilder details =
                        new StringBuilder();

                if (paper.getSubject().getCode() != null) {

                    details.append(
                            "Subject Code: "
                    );

                    details.append(
                            paper.getSubject().getCode()
                    );
                }

                if (paper.getSubject().getSemester() != null) {

                    if (details.length() > 0) {
                        details.append("    ");
                    }

                    details.append(
                            "Semester: "
                    );

                    details.append(
                            paper.getSubject()
                                    .getSemester()
                    );
                }

                if (paper.getSubject()
                        .getAcademicYear() != null) {

                    if (details.length() > 0) {
                        details.append("    ");
                    }

                    details.append(
                            "Academic Year: "
                    );

                    details.append(
                            paper.getSubject()
                                    .getAcademicYear()
                    );
                }

                if (details.length() > 0) {

                    Paragraph subjectDetails =
                            new Paragraph(
                                    details.toString(),
                                    normalFont
                            );

                    subjectDetails.setAlignment(
                            Element.ALIGN_CENTER
                    );

                    document.add(
                            subjectDetails
                    );
                }
            }

            // =================================================
            // EXAM SUMMARY
            // =================================================

            Paragraph summary =
                    new Paragraph(
                            "Total Questions: "
                                    + paper.getTotalQuestions()
                                    + "     "
                                    + "Maximum Marks: "
                                    + paper.getTotalMarks(),
                            normalFont
                    );

            summary.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(summary);

            // =================================================
            // SPACING
            // =================================================

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // INSTRUCTIONS
            // =================================================

            Paragraph instructionTitle =
                    new Paragraph(
                            "Instructions:",
                            titleFont
                    );

            document.add(
                    instructionTitle
            );

            document.add(
                    new Paragraph(
                            "1. Answer all questions.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "2. Read each question carefully.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "3. Marks are indicated against each question.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // QUESTIONS
            // =================================================

            List<PaperQuestion> questions =
                    paper.getQuestions()
                            .stream()
                            .sorted(
                                    Comparator.comparing(
                                            PaperQuestion::getQuestionOrder
                                    )
                            )
                            .toList();

            for (PaperQuestion paperQuestion :
                    questions) {

                if (paperQuestion.getQuestion() == null) {
                    continue;
                }

                String questionText =
                        paperQuestion
                                .getQuestion()
                                .getQuestionText();

                Integer marks =
                        paperQuestion
                                .getQuestion()
                                .getMarks();

                String questionNumber =
                        String.valueOf(
                                paperQuestion
                                        .getQuestionOrder()
                        );

                String text =
                        "Q"
                                + questionNumber
                                + ". "
                                + questionText
                                + "    ["
                                + marks
                                + " Marks]";

                Paragraph question =
                        new Paragraph(
                                text,
                                questionFont
                        );

                question.setSpacingAfter(
                        12
                );

                document.add(question);
            }

            // =================================================
            // FOOTER
            // =================================================

            document.add(
                    new Paragraph(" ")
            );

            Paragraph footer =
                    new Paragraph(
                            "— End of Question Paper —",
                            normalFont
                    );

            footer.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(footer);

            // =================================================
            // CLOSE PDF
            // =================================================

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {

            throw new RuntimeException(
                    "Failed to generate question paper PDF",
                    e
            );
        }
    }
}
package com.edupaper.service;

import com.edupaper.dto.ai.AIGeneratedQuestionResponse;
import com.edupaper.dto.ai.GenerateQuestionRequest;
import com.edupaper.entity.DocumentUnitContent;
import com.edupaper.exception.BadRequestException;
import com.edupaper.repository.DocumentUnitContentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AIQuestionGenerationService {

    private final DocumentUnitContentRepository
            documentUnitContentRepository;

    private final OllamaService ollamaService;

    private final ObjectMapper objectMapper;


    @Transactional(readOnly = true)
    public AIGeneratedQuestionResponse
    generateQuestion(
            GenerateQuestionRequest request,
            Long userId
    ) {

        DocumentUnitContent unitContent =
                documentUnitContentRepository
                        .findById(
                                request
                                        .getDocumentUnitContentId()
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Unit content not found."
                                )
                        );


        /*
         * Security check:
         *
         * The document must belong to
         * the current teacher.
         */
        if (!unitContent
                .getDocument()
                .getUploadedBy()
                .getId()
                .equals(userId)) {

            throw new BadRequestException(
                    "You do not have access to this document."
            );
        }


        String content =
                unitContent.getContent();


        /*
         * Keep the prompt focused.
         *
         * Later we can add chunking/RAG for
         * very large unit content.
         */
        String prompt =
                buildPrompt(
                        unitContent,
                        request
                );


        String aiResponse =
                ollamaService.generateQuestion(
                        prompt
                );


        return parseResponse(
                aiResponse,
                unitContent,
                request
        );
    }


    private String buildPrompt(
            DocumentUnitContent unitContent,
            GenerateQuestionRequest request
    ) {

        return """
                You are an academic question paper generator.

                Generate exactly ONE examination question.

                IMPORTANT RULES:

                1. Use ONLY the supplied study material.
                2. Do not introduce concepts that are not supported
                   by the supplied material.
                3. Follow the requested question type.
                4. Follow the requested difficulty.
                5. Follow the requested Bloom's taxonomy level.
                6. Follow the requested marks.
                7. Return ONLY valid JSON.
                8. Do not use Markdown.
                9. Do not add comments outside the JSON.

                COURSE UNIT:
                %s

                UNIT NUMBER:
                %d

                UNIT CONTENT:
                %s

                REQUIRED QUESTION TYPE:
                %s

                REQUIRED DIFFICULTY:
                %s

                REQUIRED BLOOM LEVEL:
                %s

                REQUIRED MARKS:
                %d

                Return exactly this JSON structure:

                {
                  "questionText": "string",
                  "answer": "string",
                  "explanation": "string"
                }
                """
                .formatted(
                        unitContent
                                .getUnit()
                                .getTitle(),

                        unitContent
                                .getUnit()
                                .getUnitNumber(),

                        contentForPrompt(
                                unitContent
                                        .getContent()
                        ),

                        request
                                .getQuestionType()
                                .name(),

                        request
                                .getDifficulty()
                                .name(),

                        request
                                .getBloomLevel()
                                .name(),

                        request.getMarks()
                );
    }


    private String contentForPrompt(
            String content
    ) {

        /*
         * Prevent an accidentally enormous prompt
         * during this first implementation.
         *
         * We'll implement proper chunking later.
         */
        int maxCharacters = 30000;

        if (content.length() <=
                maxCharacters) {

            return content;
        }

        return content.substring(
                0,
                maxCharacters
        );
    }


    private AIGeneratedQuestionResponse
    parseResponse(
            String aiResponse,
            DocumentUnitContent unitContent,
            GenerateQuestionRequest request
    ) {

        try {

            JsonNode json =
                    objectMapper.readTree(
                            aiResponse
                    );


            String questionText =
                    getRequiredText(
                            json,
                            "questionText"
                    );

            String answer =
                    getRequiredText(
                            json,
                            "answer"
                    );

            String explanation =
                    getOptionalText(
                            json,
                            "explanation"
                    );


            return AIGeneratedQuestionResponse
                    .builder()
                    .questionText(
                            questionText
                    )
                    .answer(answer)
                    .explanation(
                            explanation
                    )
                    .questionType(
                            request
                                    .getQuestionType()
                    )
                    .difficulty(
                            request
                                    .getDifficulty()
                    )
                    .marks(
                            request.getMarks()
                    )
                    .bloomLevel(
                            request
                                    .getBloomLevel()
                    )
                    .unitId(
                            unitContent
                                    .getUnit()
                                    .getId()
                    )
                    .unitNumber(
                            unitContent
                                    .getUnit()
                                    .getUnitNumber()
                    )
                    .unitTitle(
                            unitContent
                                    .getUnit()
                                    .getTitle()
                    )
                    .source(
                            "PDF_AI_GENERATED"
                    )
                    .build();


        } catch (Exception e) {

            throw new BadRequestException(
                    "AI returned invalid JSON: "
                            + aiResponse
            );
        }
    }


    private String getRequiredText(
            JsonNode json,
            String field
    ) {

        JsonNode node =
                json.get(field);

        if (node == null ||
                node.isNull() ||
                node.asText().isBlank()) {

            throw new BadRequestException(
                    "AI response is missing: "
                            + field
            );
        }

        return node.asText().trim();
    }


    private String getOptionalText(
            JsonNode json,
            String field
    ) {

        JsonNode node =
                json.get(field);

        if (node == null ||
                node.isNull()) {

            return "";
        }

        return node.asText().trim();
    }
}
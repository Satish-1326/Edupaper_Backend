import { useEffect, useMemo, useState } from "react";
import api from "../services/api";

const emptyForm = {
  questionText: "",
  answer: "",
  explanation: "",
  questionType: "SHORT_ANSWER",
  difficulty: "MEDIUM",
  marks: 1,
  bloomLevel: "L1_REMEMBER",
  subjectId: "",
  unitId: "",
  topicId: "",
  courseOutcomeId: "",
  source: "",
  tags: "",
  options: [],
};

const Questions = () => {
  const [questions, setQuestions] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [units, setUnits] = useState([]);
  const [topics, setTopics] = useState([]);
  const [courseOutcomes, setCourseOutcomes] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [search, setSearch] = useState("");
  const [bloomFilter, setBloomFilter] = useState("");
  const [difficultyFilter, setDifficultyFilter] = useState("");
  const [typeFilter, setTypeFilter] = useState("");
  const [subjectFilter, setSubjectFilter] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    loadQuestions();
    loadSubjects();
  }, []);

  const loadQuestions = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/questions");
      setQuestions(response.data);
    } catch (error) {
      console.error("Failed to load questions:", error);

      setError(
        error.response?.data?.message ||
          "Failed to load questions"
      );
    } finally {
      setLoading(false);
    }
  };

  const loadSubjects = async () => {
    try {
      const response = await api.get("/subjects");
      setSubjects(response.data);
    } catch (error) {
      console.error("Failed to load subjects:", error);
    }
  };

  const loadUnits = async (subjectId) => {
    if (!subjectId) {
      setUnits([]);
      return;
    }

    try {
      const response = await api.get(
        `/subjects/${subjectId}/units`
      );

      setUnits(response.data);
    } catch (error) {
      console.error("Failed to load units:", error);
      setUnits([]);
    }
  };

  const loadTopics = async (unitId) => {
    if (!unitId) {
      setTopics([]);
      return;
    }

    try {
      const response = await api.get(
        `/units/${unitId}/topics`
      );

      setTopics(response.data);
    } catch (error) {
      console.error("Failed to load topics:", error);
      setTopics([]);
    }
  };

const loadCourseOutcomes = async (subjectId) => {
  if (!subjectId) {
    setCourseOutcomes([]);
    return;
  }

  try {
    const response = await api.get(
      `/subjects/${subjectId}/course-outcomes`
    );

    setCourseOutcomes(response.data);
  } catch (error) {
    console.error(
      "Failed to load course outcomes:",
      error
    );

    setCourseOutcomes([]);
  }
};

  const openAddForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setUnits([]);
    setTopics([]);
    setCourseOutcomes([]);
    setError("");
    setSuccess("");
    setShowForm(true);
  };

  const openEditForm = async (question) => {
    setEditingId(question.id);
    setError("");
    setSuccess("");
    setShowForm(true);

    const subjectId =
      question.subjectId || question.subject?.id || "";

    const unitId =
      question.unitId || question.unit?.id || "";

    const topicId =
      question.topicId || question.topic?.id || "";

    const courseOutcomeId =
      question.courseOutcomeId ||
      question.courseOutcome?.id ||
      "";

    const existingOptions = (question.options || []).map(
      (option) => ({
        optionLabel: option.optionLabel,
        optionText: option.optionText,
        correct: option.correct,
      })
    );

    setForm({
      questionText: question.questionText || "",
      answer: question.answer || "",
      explanation: question.explanation || "",
      questionType:
        question.questionType || "SHORT_ANSWER",
      difficulty:
        question.difficulty || "MEDIUM",
      marks: question.marks || 1,
      bloomLevel:
        question.bloomLevel || "L1_REMEMBER",
      subjectId: subjectId ? String(subjectId) : "",
      unitId: unitId ? String(unitId) : "",
      topicId: topicId ? String(topicId) : "",
      courseOutcomeId: courseOutcomeId
        ? String(courseOutcomeId)
        : "",
      source: question.source || "",
      tags: question.tags || "",
      options: existingOptions,
    });

    if (subjectId) {
      await loadUnits(subjectId);
      await loadCourseOutcomes(subjectId);
    }

    if (unitId) {
      await loadTopics(unitId);
    }
  };

  const closeForm = () => {
    if (saving) return;

    setShowForm(false);
    setEditingId(null);
    setForm(emptyForm);
    setUnits([]);
    setTopics([]);
    setCourseOutcomes([]);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleSubjectChange = async (e) => {
    const subjectId = e.target.value;

    setForm((previous) => ({
      ...previous,
      subjectId,
      unitId: "",
      topicId: "",
      courseOutcomeId: "",
    }));

    setUnits([]);
    setTopics([]);
    setCourseOutcomes([]);

    if (subjectId) {
      await loadUnits(subjectId);
      await loadCourseOutcomes(subjectId);
    }
  };

  const handleUnitChange = async (e) => {
    const unitId = e.target.value;

    setForm((previous) => ({
      ...previous,
      unitId,
      topicId: "",
    }));

    setTopics([]);

    if (unitId) {
      await loadTopics(unitId);
    }
  };

  const handleOptionChange = (
    index,
    field,
    value
  ) => {
    setForm((previous) => {

      const updatedOptions = [...previous.options];

      updatedOptions[index] = {
        ...updatedOptions[index],
        [field]: value,
      };

      return {
        ...previous,
        options: updatedOptions,
      };
    });
  };

  const addOption = () => {
    setForm((previous) => ({
      ...previous,
      options: [
        ...previous.options,
        {
          optionLabel: getOptionLabel(
            previous.options.length
          ),
          optionText: "",
          correct: false,
        },
      ],
    }));
  };

  const removeOption = (index) => {
    setForm((previous) => ({
      ...previous,
      options: previous.options.filter(
        (_, optionIndex) => optionIndex !== index
      ),
    }));
  };

  const handleCorrectChange = (index) => {
    setForm((previous) => {

      const updatedOptions = previous.options.map(
        (option, optionIndex) => {

          if (
            previous.questionType === "MCQ" ||
            previous.questionType === "TRUE_FALSE"
          ) {
            return {
              ...option,
              correct: optionIndex === index,
            };
          }

          return optionIndex === index
            ? {
                ...option,
                correct: !option.correct,
              }
            : option;
        }
      );

      return {
        ...previous,
        options: updatedOptions,
      };
    });
  };

  const handleQuestionTypeChange = (e) => {
    const questionType = e.target.value;

    let options = [];

    if (questionType === "TRUE_FALSE") {
      options = [
        {
          optionLabel: "A",
          optionText: "True",
          correct: false,
        },
        {
          optionLabel: "B",
          optionText: "False",
          correct: false,
        },
      ];
    }

    if (questionType === "MCQ") {
      options = [
        {
          optionLabel: "A",
          optionText: "",
          correct: false,
        },
        {
          optionLabel: "B",
          optionText: "",
          correct: false,
        },
        {
          optionLabel: "C",
          optionText: "",
          correct: false,
        },
        {
          optionLabel: "D",
          optionText: "",
          correct: false,
        },
      ];
    }

    if (questionType === "MSQ") {
      options = [
        {
          optionLabel: "A",
          optionText: "",
          correct: false,
        },
        {
          optionLabel: "B",
          optionText: "",
          correct: false,
        },
        {
          optionLabel: "C",
          optionText: "",
          correct: false,
        },
        {
          optionLabel: "D",
          optionText: "",
          correct: false,
        },
      ];
    }

    setForm((previous) => ({
      ...previous,
      questionType,
      options,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    if (!form.subjectId) {
      setError("Please select a subject.");
      return;
    }

    if (!form.unitId) {
      setError("Please select a unit.");
      return;
    }

    if (
      ["MCQ", "MSQ", "TRUE_FALSE"].includes(
        form.questionType
      )
    ) {
      if (form.options.length === 0) {
        setError("Please add question options.");
        return;
      }

      const emptyOption = form.options.some(
        (option) => !option.optionText.trim()
      );

      if (emptyOption) {
        setError(
          "Please enter text for every option."
        );
        return;
      }

      const correctCount = form.options.filter(
        (option) => option.correct
      ).length;

      if (
        form.questionType === "MCQ" &&
        correctCount !== 1
      ) {
        setError(
          "MCQ must have exactly one correct option."
        );
        return;
      }

      if (
        form.questionType === "MSQ" &&
        correctCount < 2
      ) {
        setError(
          "MSQ must have at least two correct options."
        );
        return;
      }

      if (
        form.questionType === "TRUE_FALSE" &&
        correctCount !== 1
      ) {
        setError(
          "True/False must have exactly one correct option."
        );
        return;
      }
    }

    try {
      setSaving(true);

      const requestData = {
        questionText: form.questionText,
        answer: form.answer,
        explanation: form.explanation,
        questionType: form.questionType,
        difficulty: form.difficulty,
        marks: Number(form.marks),
        bloomLevel: form.bloomLevel,

        subjectId: Number(form.subjectId),
        unitId: Number(form.unitId),

        topicId: form.topicId
          ? Number(form.topicId)
          : null,

        courseOutcomeId: form.courseOutcomeId
          ? Number(form.courseOutcomeId)
          : null,

        source: form.source,
        tags: form.tags,

        options: form.options,
      };

      if (editingId) {
        await api.put(
          `/questions/${editingId}`,
          requestData
        );

        setSuccess(
          "Question updated successfully."
        );
      } else {
        await api.post(
          "/questions",
          requestData
        );

        setSuccess(
          "Question created successfully."
        );
      }

      await loadQuestions();

      setTimeout(() => {
        closeForm();
      }, 700);

    } catch (error) {
      console.error(
        "Question save error:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to save question."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (questionId) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this question?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setSuccess("");

      await api.delete(
        `/questions/${questionId}`
      );

      setQuestions((previous) =>
        previous.filter(
          (question) =>
            question.id !== questionId
        )
      );

      setSuccess(
        "Question deleted successfully."
      );

    } catch (error) {
      console.error(
        "Delete question error:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to delete question."
      );
    }
  };

  const filteredQuestions = useMemo(() => {
    return questions.filter((question) => {

      const matchesSearch =
        !search ||
        question.questionText
          ?.toLowerCase()
          .includes(search.toLowerCase());

      const matchesBloom =
        !bloomFilter ||
        question.bloomLevel === bloomFilter;

      const matchesDifficulty =
        !difficultyFilter ||
        question.difficulty === difficultyFilter;

      const matchesType =
        !typeFilter ||
        question.questionType === typeFilter;

      const matchesSubject =
        !subjectFilter ||
        String(question.subjectId) ===
          subjectFilter ||
        String(question.subject?.id) ===
          subjectFilter;

      return (
        matchesSearch &&
        matchesBloom &&
        matchesDifficulty &&
        matchesType &&
        matchesSubject
      );
    });
  }, [
    questions,
    search,
    bloomFilter,
    difficultyFilter,
    typeFilter,
    subjectFilter,
  ]);

  const clearFilters = () => {
    setSearch("");
    setBloomFilter("");
    setDifficultyFilter("");
    setTypeFilter("");
    setSubjectFilter("");
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">

      <div className="max-w-7xl mx-auto">

        {/* Header */}

        <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4 mb-8">

          <div>
            <h1 className="text-3xl font-bold text-slate-800">
              Question Bank
            </h1>

            <p className="text-slate-500 mt-1">
              Manage and organize your examination questions.
            </p>
          </div>

          <button
            onClick={openAddForm}
            className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-3 rounded-lg font-semibold"
          >
            + Add Question
          </button>

        </div>

        {/* Messages */}

        {error && (
          <div className="mb-5 bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {success && (
          <div className="mb-5 bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg">
            {success}
          </div>
        )}

        {/* Filters */}

        <div className="bg-white rounded-xl shadow-sm border p-5 mb-6">

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">

            <div className="lg:col-span-2">

              <label className="block text-sm font-medium text-slate-700 mb-2">
                Search
              </label>

              <input
                type="text"
                value={search}
                onChange={(e) =>
                  setSearch(e.target.value)
                }
                placeholder="Search questions..."
                className="w-full border border-slate-300 rounded-lg px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-500"
              />

            </div>

            <FilterSelect
              label="Subject"
              value={subjectFilter}
              onChange={setSubjectFilter}
              options={subjects.map(
                (subject) => ({
                  value: String(subject.id),
                  label: subject.name,
                })
              )}
            />

            <FilterSelect
              label="Bloom Level"
              value={bloomFilter}
              onChange={setBloomFilter}
              options={[
                {
                  value: "L1_REMEMBER",
                  label: "L1 Remember",
                },
                {
                  value: "L2_UNDERSTAND",
                  label: "L2 Understand",
                },
                {
                  value: "L3_APPLY",
                  label: "L3 Apply",
                },
                {
                  value: "L4_ANALYZE",
                  label: "L4 Analyze",
                },
                {
                  value: "L5_EVALUATE",
                  label: "L5 Evaluate",
                },
                {
                  value: "L6_CREATE",
                  label: "L6 Create",
                },
              ]}
            />

            <FilterSelect
              label="Difficulty"
              value={difficultyFilter}
              onChange={setDifficultyFilter}
              options={[
                {
                  value: "EASY",
                  label: "Easy",
                },
                {
                  value: "MEDIUM",
                  label: "Medium",
                },
                {
                  value: "HARD",
                  label: "Hard",
                },
              ]}
            />

          </div>

          <div className="flex flex-wrap items-end gap-4 mt-4">

            <div className="w-full md:w-64">

              <FilterSelect
                label="Question Type"
                value={typeFilter}
                onChange={setTypeFilter}
                options={[
                  {
                    value: "MCQ",
                    label: "MCQ",
                  },
                  {
                    value: "MSQ",
                    label: "MSQ",
                  },
                  {
                    value: "TRUE_FALSE",
                    label: "True / False",
                  },
                  {
                    value: "FILL_IN_THE_BLANK",
                    label: "Fill in the Blank",
                  },
                  {
                    value: "SHORT_ANSWER",
                    label: "Short Answer",
                  },
                  {
                    value: "LONG_ANSWER",
                    label: "Long Answer",
                  },
                  {
                    value: "DESCRIPTIVE",
                    label: "Descriptive",
                  },
                  {
                    value: "NUMERICAL",
                    label: "Numerical",
                  },
                  {
                    value: "CASE_STUDY",
                    label: "Case Study",
                  },
                ]}
              />

            </div>

            <button
              onClick={clearFilters}
              className="px-4 py-2.5 border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50"
            >
              Clear Filters
            </button>

          </div>

        </div>

        {/* Count */}

        <div className="flex justify-between items-center mb-4">

          <p className="text-sm text-slate-500">
            Showing{" "}
            <span className="font-semibold text-slate-700">
              {filteredQuestions.length}
            </span>{" "}
            of{" "}
            <span className="font-semibold text-slate-700">
              {questions.length}
            </span>{" "}
            questions
          </p>

        </div>

        {/* Questions */}

        {loading ? (

          <div className="bg-white rounded-xl border p-10 text-center">
            Loading questions...
          </div>

        ) : filteredQuestions.length === 0 ? (

          <div className="bg-white rounded-xl border p-10 text-center">

            <div className="text-5xl mb-4">
              ❓
            </div>

            <h2 className="text-xl font-semibold text-slate-700">
              No questions found
            </h2>

            <p className="text-slate-500 mt-2">
              Try changing your search or filters.
            </p>

          </div>

        ) : (

          <div className="space-y-4">

            {filteredQuestions.map(
              (question, index) => (

                <QuestionCard
                  key={question.id}
                  question={question}
                  index={index}
                  onDelete={handleDelete}
                  onEdit={openEditForm}
                />

              )
            )}

          </div>

        )}

      </div>

      {/* Question Modal */}

      {showForm && (
        <QuestionFormModal
          form={form}
          editingId={editingId}
          saving={saving}
          error={error}
          subjects={subjects}
          units={units}
          topics={topics}
          courseOutcomes={courseOutcomes}
          onClose={closeForm}
          onChange={handleChange}
          onSubjectChange={handleSubjectChange}
          onUnitChange={handleUnitChange}
          onQuestionTypeChange={
            handleQuestionTypeChange
          }
          onOptionChange={handleOptionChange}
          onCorrectChange={handleCorrectChange}
          onAddOption={addOption}
          onRemoveOption={removeOption}
          onSubmit={handleSubmit}
        />
      )}

    </div>
  );
};


/* Question Form */

const QuestionFormModal = ({
  form,
  editingId,
  saving,
  error,
  subjects,
  units,
  topics,
  courseOutcomes,
  onClose,
  onChange,
  onSubjectChange,
  onUnitChange,
  onQuestionTypeChange,
  onOptionChange,
  onCorrectChange,
  onAddOption,
  onRemoveOption,
  onSubmit,
}) => {

  const needsOptions = [
    "MCQ",
    "MSQ",
    "TRUE_FALSE",
  ].includes(form.questionType);

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">

      <div className="bg-white rounded-2xl shadow-xl w-full max-w-4xl max-h-[92vh] overflow-y-auto">

        {/* Modal Header */}

        <div className="sticky top-0 bg-white border-b p-6 z-10">

          <div className="flex justify-between items-center">

            <div>

              <h2 className="text-2xl font-bold text-slate-800">
                {editingId
                  ? "Edit Question"
                  : "Add Question"}
              </h2>

              <p className="text-sm text-slate-500 mt-1">
                Create a structured examination question.
              </p>

            </div>

            <button
              onClick={onClose}
              disabled={saving}
              className="text-slate-400 hover:text-slate-700 text-2xl"
            >
              ×
            </button>

          </div>

        </div>

        <form
          onSubmit={onSubmit}
          className="p-6 space-y-6"
        >

          {/* Question Text */}

          <div>

            <label className="block text-sm font-medium text-slate-700 mb-2">
              Question Text *
            </label>

            <textarea
              name="questionText"
              value={form.questionText}
              onChange={onChange}
              rows="4"
              required
              placeholder="Enter the question..."
              className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
            />

          </div>

          {/* Basic Configuration */}

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">

            <SelectField
              label="Question Type"
              name="questionType"
              value={form.questionType}
              onChange={onQuestionTypeChange}
              options={[
                ["MCQ", "MCQ"],
                ["MSQ", "MSQ"],
                [
                  "TRUE_FALSE",
                  "True / False",
                ],
                [
                  "FILL_IN_THE_BLANK",
                  "Fill in the Blank",
                ],
                [
                  "SHORT_ANSWER",
                  "Short Answer",
                ],
                [
                  "LONG_ANSWER",
                  "Long Answer",
                ],
                [
                  "DESCRIPTIVE",
                  "Descriptive",
                ],
                [
                  "NUMERICAL",
                  "Numerical",
                ],
                [
                  "CASE_STUDY",
                  "Case Study",
                ],
              ]}
            />

            <SelectField
              label="Difficulty"
              name="difficulty"
              value={form.difficulty}
              onChange={onChange}
              options={[
                ["EASY", "Easy"],
                ["MEDIUM", "Medium"],
                ["HARD", "Hard"],
              ]}
            />

            <SelectField
              label="Bloom Level"
              name="bloomLevel"
              value={form.bloomLevel}
              onChange={onChange}
              options={[
                [
                  "L1_REMEMBER",
                  "L1 - Remember",
                ],
                [
                  "L2_UNDERSTAND",
                  "L2 - Understand",
                ],
                [
                  "L3_APPLY",
                  "L3 - Apply",
                ],
                [
                  "L4_ANALYZE",
                  "L4 - Analyze",
                ],
                [
                  "L5_EVALUATE",
                  "L5 - Evaluate",
                ],
                [
                  "L6_CREATE",
                  "L6 - Create",
                ],
              ]}
            />

          </div>

          {/* Marks */}

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">

            <InputField
              label="Marks"
              name="marks"
              type="number"
              min="1"
              value={form.marks}
              onChange={onChange}
              required
            />

            <SelectField
              label="Subject"
              name="subjectId"
              value={form.subjectId}
              onChange={onSubjectChange}
              options={subjects.map(
                (subject) => [
                  String(subject.id),
                  `${subject.name} (${subject.code})`,
                ]
              )}
              required
            />

            <SelectField
              label="Unit"
              name="unitId"
              value={form.unitId}
              onChange={onUnitChange}
              options={units.map(
                (unit) => [
                  String(unit.id),
                  `Unit ${unit.unitNumber}: ${unit.title}`,
                ]
              )}
              required
            />

          </div>

          {/* Topic + CO */}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

            <SelectField
              label="Topic"
              name="topicId"
              value={form.topicId}
              onChange={onChange}
              options={topics.map(
                (topic) => [
                  String(topic.id),
                  topic.name,
                ]
              )}
            />

            <SelectField
              label="Course Outcome"
              name="courseOutcomeId"
              value={form.courseOutcomeId}
              onChange={onChange}
              options={courseOutcomes.map(
                (co) => [
                  String(co.id),
                  `CO${co.coNumber} - ${
                    co.description ||
                    co.outcome ||
                    ""
                  }`,
                ]
              )}
            />

          </div>

          {/* Options */}

          {needsOptions && (

            <div className="border rounded-xl p-5 bg-slate-50">

              <div className="flex justify-between items-center mb-4">

                <div>

                  <h3 className="font-bold text-slate-800">
                    Options
                  </h3>

                  <p className="text-sm text-slate-500">
                    Select the correct answer.
                  </p>

                </div>

                {form.questionType !==
                  "TRUE_FALSE" && (
                  <button
                    type="button"
                    onClick={onAddOption}
                    className="text-sm bg-blue-600 text-white px-3 py-2 rounded-lg"
                  >
                    + Add Option
                  </button>
                )}

              </div>

              <div className="space-y-3">

                {form.options.map(
                  (option, index) => (

                    <div
                      key={index}
                      className="flex gap-3 items-center"
                    >

                      <span className="w-8 font-bold text-slate-600">
                        {option.optionLabel}
                      </span>

                      <input
                        type="text"
                        value={option.optionText}
                        onChange={(e) =>
                          onOptionChange(
                            index,
                            "optionText",
                            e.target.value
                          )
                        }
                        placeholder={`Option ${option.optionLabel}`}
                        className="flex-1 border border-slate-300 rounded-lg px-3 py-2.5"
                        required
                      />

                      <label className="flex items-center gap-2 text-sm whitespace-nowrap">

                        <input
                          type="checkbox"
                          checked={option.correct}
                          onChange={() =>
                            onCorrectChange(index)
                          }
                        />

                        Correct

                      </label>

                      {form.questionType !==
                        "TRUE_FALSE" && (
                        <button
                          type="button"
                          onClick={() =>
                            onRemoveOption(index)
                          }
                          className="text-red-500 px-2"
                        >
                          ×
                        </button>
                      )}

                    </div>

                  )
                )}

              </div>

            </div>

          )}

          {/* Answer */}

          <div>

            <label className="block text-sm font-medium text-slate-700 mb-2">
              Answer
            </label>

            <textarea
              name="answer"
              value={form.answer}
              onChange={onChange}
              rows="3"
              placeholder="Enter the answer..."
              className="w-full border border-slate-300 rounded-lg px-4 py-3"
            />

          </div>

          {/* Explanation */}

          <div>

            <label className="block text-sm font-medium text-slate-700 mb-2">
              Explanation
            </label>

            <textarea
              name="explanation"
              value={form.explanation}
              onChange={onChange}
              rows="3"
              placeholder="Optional explanation..."
              className="w-full border border-slate-300 rounded-lg px-4 py-3"
            />

          </div>

          {/* Source + Tags */}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

            <InputField
              label="Source"
              name="source"
              value={form.source}
              onChange={onChange}
              placeholder="Manual / Book / AI"
            />

            <InputField
              label="Tags"
              name="tags"
              value={form.tags}
              onChange={onChange}
              placeholder="regression, ml, prediction"
            />

          </div>

          {/* Error */}

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          {/* Buttons */}

          <div className="flex justify-end gap-3 pt-3 border-t">

            <button
              type="button"
              onClick={onClose}
              disabled={saving}
              className="px-5 py-2.5 border border-slate-300 rounded-lg text-slate-700"
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={saving}
              className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-semibold disabled:opacity-50"
            >
              {saving
                ? "Saving..."
                : editingId
                ? "Update Question"
                : "Create Question"}
            </button>

          </div>

        </form>

      </div>

    </div>
  );
};


/* Question Card */

const QuestionCard = ({
  question,
  index,
  onDelete,
  onEdit,
}) => {

  return (
    <div className="bg-white rounded-xl border shadow-sm p-6">

      <div className="flex flex-col md:flex-row md:justify-between gap-5">

        <div className="flex-1">

          <div className="flex flex-wrap items-center gap-2 mb-3">

            <span className="text-sm font-bold text-slate-500">
              Q{index + 1}
            </span>

            <Badge
              text={formatBloom(
                question.bloomLevel
              )}
              type="blue"
            />

            <Badge
              text={formatDifficulty(
                question.difficulty
              )}
              type="gray"
            />

            <Badge
              text={formatQuestionType(
                question.questionType
              )}
              type="purple"
            />

            <Badge
              text={`${question.marks} Mark${
                question.marks === 1
                  ? ""
                  : "s"
              }`}
              type="green"
            />

          </div>

          <h3 className="text-lg font-semibold text-slate-800">
            {question.questionText}
          </h3>

          {question.options &&
            question.options.length > 0 && (

              <div className="mt-4 space-y-2">

                {question.options.map(
                  (option) => (

                    <div
                      key={option.id}
                      className="bg-slate-50 border rounded-lg px-4 py-2 text-sm text-slate-700"
                    >
                      <span className="font-semibold">
                        {option.optionLabel}.
                      </span>{" "}
                      {option.optionText}
                    </div>

                  )
                )}

              </div>

            )}

        </div>

        <div className="flex md:flex-col gap-2">

          <button
            onClick={() => onEdit(question)}
            className="px-4 py-2 border border-blue-500 text-blue-600 rounded-lg hover:bg-blue-50"
          >
            Edit
          </button>

          <button
            onClick={() =>
              onDelete(question.id)
            }
            className="px-4 py-2 border border-red-500 text-red-500 rounded-lg hover:bg-red-50"
          >
            Delete
          </button>

        </div>

      </div>

    </div>
  );
};


/* Input */

const InputField = ({
  label,
  name,
  type = "text",
  value,
  onChange,
  placeholder,
  required = false,
  min,
}) => {

  return (
    <div>

      <label className="block text-sm font-medium text-slate-700 mb-2">
        {label}
      </label>

      <input
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        min={min}
        className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
      />

    </div>
  );
};


/* Select */

const SelectField = ({
  label,
  name,
  value,
  onChange,
  options,
  required = false,
}) => {

  return (
    <div>

      <label className="block text-sm font-medium text-slate-700 mb-2">
        {label}
      </label>

      <select
        name={name}
        value={value}
        onChange={onChange}
        required={required}
        className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500 bg-white"
      >

        {!required && (
          <option value="">
            Select {label}
          </option>
        )}

        {required && (
          <option value="">
            Select {label}
          </option>
        )}

        {options.map(
          ([optionValue, optionLabel]) => (
            <option
              key={optionValue}
              value={optionValue}
            >
              {optionLabel}
            </option>
          )
        )}

      </select>

    </div>
  );
};


/* Filter */

const FilterSelect = ({
  label,
  value,
  onChange,
  options,
}) => {

  return (
    <div>

      <label className="block text-sm font-medium text-slate-700 mb-2">
        {label}
      </label>

      <select
        value={value}
        onChange={(e) =>
          onChange(e.target.value)
        }
        className="w-full border border-slate-300 rounded-lg px-3 py-2.5 outline-none focus:ring-2 focus:ring-blue-500 bg-white"
      >

        <option value="">
          All
        </option>

        {options.map((option) => (
          <option
            key={option.value}
            value={option.value}
          >
            {option.label}
          </option>
        ))}

      </select>

    </div>
  );
};


/* Badge */

const Badge = ({ text, type }) => {

  const styles = {
    blue: "bg-blue-50 text-blue-700",
    gray: "bg-slate-100 text-slate-700",
    purple: "bg-purple-50 text-purple-700",
    green: "bg-green-50 text-green-700",
  };

  return (
    <span
      className={`px-2.5 py-1 rounded-full text-xs font-semibold ${styles[type]}`}
    >
      {text}
    </span>
  );
};


/* Helpers */

const getOptionLabel = (index) => {
  return String.fromCharCode(
    65 + index
  );
};

const formatBloom = (value) => {

  if (!value) return "Bloom";

  const labels = {
    L1_REMEMBER: "L1 Remember",
    L2_UNDERSTAND: "L2 Understand",
    L3_APPLY: "L3 Apply",
    L4_ANALYZE: "L4 Analyze",
    L5_EVALUATE: "L5 Evaluate",
    L6_CREATE: "L6 Create",
  };

  return labels[value] || value;
};

const formatDifficulty = (value) => {

  if (!value) return "Difficulty";

  return (
    value.charAt(0) +
    value.slice(1).toLowerCase()
  );
};

const formatQuestionType = (value) => {

  if (!value) return "Question";

  return value
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (char) =>
      char.toUpperCase()
    );
};

export default Questions;
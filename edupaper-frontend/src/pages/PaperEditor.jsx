import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

export default function PaperEditor() {
  const { paperId } = useParams();
  const navigate = useNavigate();

  const [paper, setPaper] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [newQuestionId, setNewQuestionId] = useState("");
  const [replacementIds, setReplacementIds] = useState({});

  const [allQuestions, setAllQuestions] = useState([]);
  const [showAddQuestion, setShowAddQuestion] = useState(false);

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadPaper();
    loadQuestions();
  }, [paperId]);

  const loadPaper = async () => {
    try {
      setLoading(true);

      const response = await api.get(`/papers/${paperId}`);

      setPaper(response.data);
      setError("");
    } catch (err) {
      console.error(err);
      setError(
        err.response?.data?.message ||
          "Failed to load paper."
      );
    } finally {
      setLoading(false);
    }
  };

  const loadQuestions = async () => {
    try {
      const response = await api.get("/questions");
      setAllQuestions(response.data);
    } catch (err) {
      console.error("Failed to load questions", err);
    }
  };

  const addQuestion = async () => {
    if (!newQuestionId) {
      alert("Please select a question.");
      return;
    }

    try {
      setSaving(true);

      await api.post(`/papers/${paperId}/questions`, {
        questionId: Number(newQuestionId),
      });

      setNewQuestionId("");
      setShowAddQuestion(false);

      await loadPaper();

      alert("Question added successfully.");
    } catch (err) {
      alert(
        err.response?.data?.message ||
          "Failed to add question."
      );
    } finally {
      setSaving(false);
    }
  };

  const removeQuestion = async (questionId) => {
    if (!window.confirm("Remove this question from the paper?")) {
      return;
    }

    try {
      setSaving(true);

      await api.delete(
        `/papers/${paperId}/questions/${questionId}`
      );

      await loadPaper();
    } catch (err) {
      alert(
        err.response?.data?.message ||
          "Failed to remove question."
      );
    } finally {
      setSaving(false);
    }
  };

  const replaceQuestion = async (oldQuestionId) => {
    const newId = replacementIds[oldQuestionId];

    if (!newId) {
      alert("Select a replacement question.");
      return;
    }

    try {
      setSaving(true);

      await api.put(
        `/papers/${paperId}/questions/${oldQuestionId}`,
        {
          newQuestionId: Number(newId),
        }
      );

      setReplacementIds((prev) => ({
        ...prev,
        [oldQuestionId]: "",
      }));

      await loadPaper();

      alert("Question replaced successfully.");
    } catch (err) {
      alert(
        err.response?.data?.message ||
          "Failed to replace question."
      );
    } finally {
      setSaving(false);
    }
  };

  const moveQuestion = async (index, direction) => {
    if (!paper?.questions) return;

    const questions = [...paper.questions];

    const newIndex =
      direction === "up" ? index - 1 : index + 1;

    if (newIndex < 0 || newIndex >= questions.length) {
      return;
    }

    [questions[index], questions[newIndex]] = [
      questions[newIndex],
      questions[index],
    ];

    const questionIds = questions.map(
      (question) => question.questionId
    );

    try {
      setSaving(true);

      await api.put(`/papers/${paperId}/questions/order`, {
        questionIds,
      });

      await loadPaper();
    } catch (err) {
      alert(
        err.response?.data?.message ||
          "Failed to reorder questions."
      );
    } finally {
      setSaving(false);
    }
  };

  const finalizePaper = async () => {
    if (
      !window.confirm(
        "Finalize this paper? You will no longer be able to edit it."
      )
    ) {
      return;
    }

    try {
      setSaving(true);

      await api.put(`/papers/${paperId}/finalize`);

      await loadPaper();

      alert("Paper finalized successfully.");
    } catch (err) {
      alert(
        err.response?.data?.message ||
          "Paper validation failed."
      );
    } finally {
      setSaving(false);
    }
  };

  const downloadPdf = async () => {
    try {
      const response = await api.get(
        `/papers/${paperId}/pdf`,
        {
          responseType: "blob",
        }
      );

      const blob = new Blob([response.data], {
        type: "application/pdf",
      });

      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download = `${paper?.name || "question-paper"}.pdf`;

      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(
        err.response?.data?.message ||
          "PDF download failed. Make sure the paper is finalized."
      );
    }
  };

  const availableQuestions = allQuestions.filter(
    (question) =>
      !paper?.questions?.some(
        (paperQuestion) =>
          paperQuestion.questionId === question.id
      )
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[70vh]">
        <div className="text-lg text-gray-600">
          Loading paper...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-5">
          {error}
        </div>
      </div>
    );
  }

  if (!paper) {
    return null;
  }

  const isFinalized = paper.status === "FINALIZED";

  return (
    <div className="p-6 max-w-7xl mx-auto">

      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">

        <div>
          <button
            onClick={() => navigate("/papers")}
            className="text-sm text-blue-600 hover:underline mb-2"
          >
            ← Back to Papers
          </button>

          <h1 className="text-3xl font-bold text-gray-900">
            {paper.name}
          </h1>

          <p className="text-gray-500 mt-1">
            Question Paper Editor
          </p>
        </div>

        <div className="flex gap-3">

          {!isFinalized && (
            <button
              onClick={() => setShowAddQuestion(true)}
              className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700"
            >
              + Add Question
            </button>
          )}

          {!isFinalized && (
            <button
              onClick={finalizePaper}
              disabled={saving}
              className="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700 disabled:opacity-50"
            >
              ✓ Finalize Paper
            </button>
          )}

          {isFinalized && (
            <button
              onClick={downloadPdf}
              className="px-4 py-2 rounded-lg bg-purple-600 text-white hover:bg-purple-700"
            >
              ↓ Download PDF
            </button>
          )}

        </div>
      </div>

      {/* Status */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">

        <StatCard
          title="Questions"
          value={paper.totalQuestions}
        />

        <StatCard
          title="Total Marks"
          value={paper.totalMarks}
        />

        <StatCard
          title="Current Questions"
          value={paper.questions?.length || 0}
        />

        <div className="bg-white border rounded-xl p-5 shadow-sm">
          <p className="text-sm text-gray-500">
            Status
          </p>

          <span
            className={`inline-block mt-2 px-3 py-1 rounded-full text-sm font-semibold ${
              paper.status === "FINALIZED"
                ? "bg-green-100 text-green-700"
                : paper.status === "GENERATED"
                ? "bg-blue-100 text-blue-700"
                : "bg-yellow-100 text-yellow-700"
            }`}
          >
            {paper.status}
          </span>
        </div>

      </div>

      {/* Validation warning */}
      {paper.questions?.length !== paper.totalQuestions && (
        <div className="mb-6 bg-yellow-50 border border-yellow-200 text-yellow-800 rounded-xl p-4">
          <strong>Paper incomplete:</strong>{" "}
          This paper requires {paper.totalQuestions} questions,
          but currently contains {paper.questions?.length || 0}.
        </div>
      )}

      {/* Questions */}
      <div className="space-y-4">

        {paper.questions?.map((question, index) => (

          <div
            key={question.id}
            className="bg-white border rounded-xl shadow-sm p-5"
          >

            <div className="flex gap-4">

              {/* Question number */}
              <div className="w-10 h-10 flex-shrink-0 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold">
                {index + 1}
              </div>

              <div className="flex-1">

                {/* Question header */}
                <div className="flex flex-wrap gap-2 mb-3">

                  <Badge>
                    {question.questionType}
                  </Badge>

                  <Badge>
                    {question.difficulty}
                  </Badge>

                  <Badge>
                    {question.bloomLevel}
                  </Badge>

                  <Badge>
                    {question.marks} Marks
                  </Badge>

                </div>

                <p className="text-gray-900 text-lg leading-relaxed">
                  {question.questionText}
                </p>

                {/* Actions */}
                {!isFinalized && (
                  <div className="mt-5 flex flex-wrap gap-2">

                    <button
                      onClick={() =>
                        moveQuestion(index, "up")
                      }
                      disabled={index === 0 || saving}
                      className="px-3 py-1.5 border rounded-lg text-sm hover:bg-gray-50 disabled:opacity-40"
                    >
                      ↑ Move Up
                    </button>

                    <button
                      onClick={() =>
                        moveQuestion(index, "down")
                      }
                      disabled={
                        index ===
                          paper.questions.length - 1 ||
                        saving
                      }
                      className="px-3 py-1.5 border rounded-lg text-sm hover:bg-gray-50 disabled:opacity-40"
                    >
                      ↓ Move Down
                    </button>

                    <button
                      onClick={() =>
                        removeQuestion(question.questionId)
                      }
                      disabled={saving}
                      className="px-3 py-1.5 border border-red-200 text-red-600 rounded-lg text-sm hover:bg-red-50"
                    >
                      Remove
                    </button>

                  </div>
                )}

                {/* Replace */}
                {!isFinalized && (
                  <div className="mt-4 flex flex-col md:flex-row gap-2">

                    <select
                      value={
                        replacementIds[
                          question.questionId
                        ] || ""
                      }
                      onChange={(e) =>
                        setReplacementIds((prev) => ({
                          ...prev,
                          [question.questionId]:
                            e.target.value,
                        }))
                      }
                      className="border rounded-lg px-3 py-2 text-sm flex-1"
                    >
                      <option value="">
                        Select replacement question
                      </option>

                      {availableQuestions.map((q) => (
                        <option
                          key={q.id}
                          value={q.id}
                        >
                          #{q.id} —{" "}
                          {q.questionText?.substring(
                            0,
                            80
                          )}
                        </option>
                      ))}
                    </select>

                    <button
                      onClick={() =>
                        replaceQuestion(
                          question.questionId
                        )
                      }
                      disabled={saving}
                      className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:opacity-50"
                    >
                      Replace
                    </button>

                  </div>
                )}

              </div>
            </div>
          </div>

        ))}

      </div>

      {/* Add Question Modal */}
      {showAddQuestion && !isFinalized && (

        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">

          <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl p-6">

            <div className="flex justify-between items-center mb-5">

              <h2 className="text-xl font-bold">
                Add Question
              </h2>

              <button
                onClick={() => setShowAddQuestion(false)}
                className="text-gray-500 text-xl"
              >
                ×
              </button>

            </div>

            <select
              value={newQuestionId}
              onChange={(e) =>
                setNewQuestionId(e.target.value)
              }
              className="w-full border rounded-lg p-3 mb-5"
            >
              <option value="">
                Select a question
              </option>

              {availableQuestions.map((question) => (

                <option
                  key={question.id}
                  value={question.id}
                >
                  #{question.id} —{" "}
                  {question.questionText?.substring(
                    0,
                    100
                  )}
                </option>

              ))}

            </select>

            <div className="flex justify-end gap-3">

              <button
                onClick={() =>
                  setShowAddQuestion(false)
                }
                className="px-4 py-2 border rounded-lg"
              >
                Cancel
              </button>

              <button
                onClick={addQuestion}
                disabled={saving}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Add Question
              </button>

            </div>

          </div>
        </div>

      )}

    </div>
  );
}


/* ---------------- Components ---------------- */

function StatCard({ title, value }) {
  return (
    <div className="bg-white border rounded-xl p-5 shadow-sm">
      <p className="text-sm text-gray-500">
        {title}
      </p>

      <p className="text-3xl font-bold text-gray-900 mt-2">
        {value}
      </p>
    </div>
  );
}

function Badge({ children }) {
  return (
    <span className="px-2.5 py-1 rounded-full bg-gray-100 text-gray-700 text-xs font-medium">
      {children}
    </span>
  );
}
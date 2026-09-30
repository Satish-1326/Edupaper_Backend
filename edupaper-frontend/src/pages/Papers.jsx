import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

const Papers = () => {
  const navigate = useNavigate();

  const [papers, setPapers] = useState([]);
  const [blueprints, setBlueprints] = useState([]);
  const [subjects, setSubjects] = useState([]);

  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);

  const [showModal, setShowModal] = useState(false);

  const [form, setForm] = useState({
    name: "",
    blueprintId: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError("");

      const [papersResponse, blueprintsResponse, subjectsResponse] =
        await Promise.all([
          api.get("/papers"),
          api.get("/blueprints"),
          api.get("/subjects"),
        ]);

      setPapers(papersResponse.data);
      setBlueprints(blueprintsResponse.data);
      setSubjects(subjectsResponse.data);
    } catch (error) {
      console.error("Failed to load papers:", error);

      setError(
        error.response?.data?.message ||
          "Failed to load paper data."
      );
    } finally {
      setLoading(false);
    }
  };

  const openGenerateModal = () => {
    setForm({
      name: "",
      blueprintId: "",
    });

    setError("");
    setSuccess("");
    setShowModal(true);
  };

  const closeGenerateModal = () => {
    if (generating) return;

    setShowModal(false);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleGenerate = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    if (!form.name.trim()) {
      setError("Paper name is required.");
      return;
    }

    if (!form.blueprintId) {
      setError("Please select a blueprint.");
      return;
    }

    try {
      setGenerating(true);

      const response = await api.post(
        "/papers/generate",
        {
          name: form.name,
          blueprintId: Number(form.blueprintId),
        }
      );

      const generatedPaper = response.data;

      setSuccess("Paper generated successfully.");

      setPapers((previous) => [
        generatedPaper,
        ...previous,
      ]);

      setTimeout(() => {
        setShowModal(false);

        navigate(
          `/papers/${generatedPaper.id}`
        );
      }, 700);

    } catch (error) {
      console.error(
        "Paper generation error:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to generate paper."
      );
    } finally {
      setGenerating(false);
    }
  };

  const handleDelete = async (paperId) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this paper?"
    );

    if (!confirmed) return;

    try {
      setError("");
      setSuccess("");

      await api.delete(
        `/papers/${paperId}`
      );

      setPapers((previous) =>
        previous.filter(
          (paper) => paper.id !== paperId
        )
      );

      setSuccess(
        "Paper deleted successfully."
      );
    } catch (error) {
      console.error(
        "Paper delete error:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to delete paper."
      );
    }
  };

  const getSubjectName = (subjectId) => {
    const subject = subjects.find(
      (item) => item.id === subjectId
    );

    return (
      subject?.name ||
      `Subject ${subjectId}`
    );
  };

  const getBlueprintName = (blueprintId) => {
    const blueprint = blueprints.find(
      (item) => item.id === blueprintId
    );

    return (
      blueprint?.name ||
      `Blueprint ${blueprintId}`
    );
  };

  const getStatusStyle = (status) => {
    switch (status) {
      case "FINALIZED":
        return "bg-green-100 text-green-700";

      case "GENERATED":
        return "bg-blue-100 text-blue-700";

      case "DRAFT":
        return "bg-yellow-100 text-yellow-700";

      default:
        return "bg-slate-100 text-slate-700";
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">

      <div className="max-w-7xl mx-auto">

        {/* Header */}

        <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4 mb-8">

          <div>
            <h1 className="text-3xl font-bold text-slate-800">
              Question Papers
            </h1>

            <p className="text-slate-500 mt-1">
              Generate and manage your examination papers.
            </p>
          </div>

          <button
            onClick={openGenerateModal}
            className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-3 rounded-lg font-semibold"
          >
            + Generate Paper
          </button>

        </div>

        {/* Messages */}

        {error && !showModal && (
          <div className="mb-5 bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {success && !showModal && (
          <div className="mb-5 bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg">
            {success}
          </div>
        )}

        {/* Loading */}

        {loading ? (

          <div className="bg-white rounded-xl border p-10 text-center">
            Loading papers...
          </div>

        ) : papers.length === 0 ? (

          <div className="bg-white rounded-xl border p-12 text-center">

            <div className="text-5xl mb-4">
              📄
            </div>

            <h2 className="text-xl font-semibold text-slate-700">
              No papers generated yet
            </h2>

            <p className="text-slate-500 mt-2 mb-5">
              Create a blueprint and generate your first question paper.
            </p>

            <button
              onClick={openGenerateModal}
              className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg font-semibold"
            >
              Generate Paper
            </button>

          </div>

        ) : (

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

            {papers.map((paper) => (

              <div
                key={paper.id}
                className="bg-white rounded-xl border shadow-sm p-6"
              >

                {/* Top */}

                <div className="flex justify-between items-start gap-4">

                  <div>

                    <h2 className="text-xl font-bold text-slate-800">
                      {paper.name}
                    </h2>

                    <p className="text-sm text-blue-600 mt-1">
                      {getSubjectName(
                        paper.subjectId
                      )}
                    </p>

                  </div>

                  <span
                    className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusStyle(
                      paper.status
                    )}`}
                  >
                    {paper.status}
                  </span>

                </div>

                {/* Details */}

                <div className="grid grid-cols-3 gap-3 mt-6">

                  <div className="bg-slate-50 rounded-lg p-4">
                    <p className="text-xs text-slate-500">
                      Questions
                    </p>

                    <p className="text-xl font-bold text-slate-800 mt-1">
                      {paper.totalQuestions}
                    </p>
                  </div>

                  <div className="bg-slate-50 rounded-lg p-4">
                    <p className="text-xs text-slate-500">
                      Marks
                    </p>

                    <p className="text-xl font-bold text-slate-800 mt-1">
                      {paper.totalMarks}
                    </p>
                  </div>

                  <div className="bg-slate-50 rounded-lg p-4">
                    <p className="text-xs text-slate-500">
                      Blueprint
                    </p>

                    <p className="text-sm font-semibold text-slate-700 mt-1 truncate">
                      {getBlueprintName(
                        paper.blueprintId
                      )}
                    </p>
                  </div>

                </div>

                {/* Actions */}

                <div className="flex gap-3 mt-6">

                  <button
                    onClick={() =>
                      navigate(
                        `/papers/${paper.id}`
                      )
                    }
                    className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2.5 rounded-lg font-semibold"
                  >
                    Open Editor
                  </button>

                  <button
                    onClick={() =>
                      handleDelete(paper.id)
                    }
                    disabled={
                      paper.status ===
                      "FINALIZED"
                    }
                    className="px-5 border border-red-500 text-red-500 hover:bg-red-50 disabled:opacity-40 disabled:cursor-not-allowed rounded-lg font-medium"
                  >
                    Delete
                  </button>

                </div>

              </div>

            ))}

          </div>

        )}

      </div>

      {/* Generate Modal */}

      {showModal && (

        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">

          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg">

            {/* Modal Header */}

            <div className="border-b p-6">

              <div className="flex justify-between items-center">

                <div>

                  <h2 className="text-2xl font-bold text-slate-800">
                    Generate Question Paper
                  </h2>

                  <p className="text-sm text-slate-500 mt-1">
                    Select a blueprint and generate the paper automatically.
                  </p>

                </div>

                <button
                  onClick={closeGenerateModal}
                  disabled={generating}
                  className="text-slate-400 hover:text-slate-700 text-2xl"
                >
                  ×
                </button>

              </div>

            </div>

            {/* Form */}

            <form
              onSubmit={handleGenerate}
              className="p-6 space-y-5"
            >

              {error && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
                  {error}
                </div>
              )}

              {success && (
                <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-lg">
                  {success}
                </div>
              )}

              {/* Paper Name */}

              <div>

                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Paper Name
                </label>

                <input
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Machine Learning Final Examination"
                  required
                  className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                />

              </div>

              {/* Blueprint */}

              <div>

                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Blueprint
                </label>

                <select
                  name="blueprintId"
                  value={form.blueprintId}
                  onChange={handleChange}
                  required
                  className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                >

                  <option value="">
                    Select Blueprint
                  </option>

                  {blueprints.map(
                    (blueprint) => (
                      <option
                        key={blueprint.id}
                        value={blueprint.id}
                      >
                        {blueprint.name} —{" "}
                        {blueprint.totalQuestions} Questions /{" "}
                        {blueprint.totalMarks} Marks
                      </option>
                    )
                  )}

                </select>

              </div>

              {/* Selected Blueprint Preview */}

              {form.blueprintId && (

                <BlueprintPreview
                  blueprint={blueprints.find(
                    (blueprint) =>
                      blueprint.id ===
                      Number(
                        form.blueprintId
                      )
                  )}
                />

              )}

              {/* Buttons */}

              <div className="flex justify-end gap-3 pt-3 border-t">

                <button
                  type="button"
                  onClick={closeGenerateModal}
                  disabled={generating}
                  className="px-5 py-2.5 border border-slate-300 rounded-lg text-slate-700"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={
                    generating ||
                    blueprints.length === 0
                  }
                  className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-semibold disabled:opacity-50"
                >
                  {generating
                    ? "Generating..."
                    : "Generate Paper"}
                </button>

              </div>

            </form>

          </div>

        </div>

      )}

    </div>
  );
};


/* Blueprint Preview */

const BlueprintPreview = ({
  blueprint,
}) => {

  if (!blueprint) return null;

  return (
    <div className="bg-blue-50 border border-blue-100 rounded-xl p-4">

      <h3 className="font-semibold text-blue-800 mb-3">
        Blueprint Summary
      </h3>

      <div className="grid grid-cols-2 gap-3 text-sm">

        <div>
          <span className="text-slate-500">
            Questions:
          </span>{" "}
          <strong>
            {blueprint.totalQuestions}
          </strong>
        </div>

        <div>
          <span className="text-slate-500">
            Marks:
          </span>{" "}
          <strong>
            {blueprint.totalMarks}
          </strong>
        </div>

      </div>

      {blueprint.constraints?.length >
        0 && (

        <div className="mt-4">

          <p className="text-sm font-medium text-slate-700 mb-2">
            Constraints
          </p>

          <div className="flex flex-wrap gap-2">

            {blueprint.constraints.map(
              (constraint) => (

                <span
                  key={constraint.id}
                  className="bg-white border border-blue-200 text-blue-700 px-3 py-1 rounded-full text-xs"
                >
                  {constraint.constraintType ===
                  "UNIT"
                    ? `Unit ${constraint.value}`
                    : constraint.value}
                  {" : "}
                  {constraint.requiredCount}
                </span>

              )
            )}

          </div>

        </div>

      )}

    </div>
  );
};

export default Papers;
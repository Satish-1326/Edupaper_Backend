import { useEffect, useState } from "react";
import api from "../services/api";

const emptyForm = {
  name: "",
  description: "",
  subjectId: "",
  totalQuestions: 10,
  totalMarks: 20,
  constraints: [],
};

const Blueprints = () => {
  const [blueprints, setBlueprints] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [units, setUnits] = useState([]);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState(emptyForm);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadBlueprints();
    loadSubjects();
  }, []);

  const loadBlueprints = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/blueprints");

      setBlueprints(response.data);
    } catch (error) {
      console.error(
        "Failed to load blueprints:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to load blueprints"
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
      console.error(
        "Failed to load subjects:",
        error
      );
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
      console.error(
        "Failed to load units:",
        error
      );

      setUnits([]);
    }
  };

  const openCreateForm = () => {
    setEditingId(null);

    setForm({
      ...emptyForm,
      constraints: [],
    });

    setUnits([]);
    setError("");
    setSuccess("");
    setShowForm(true);
  };

  const openEditForm = async (blueprint) => {
    setEditingId(blueprint.id);

    const subjectId = blueprint.subjectId;

    setForm({
      name: blueprint.name || "",
      description: blueprint.description || "",
      subjectId: subjectId
        ? String(subjectId)
        : "",
      totalQuestions:
        blueprint.totalQuestions || 0,
      totalMarks:
        blueprint.totalMarks || 0,
      constraints:
        (blueprint.constraints || []).map(
          (constraint) => ({
            constraintType:
              constraint.constraintType,
            value: String(
              constraint.value
            ),
            requiredCount:
              constraint.requiredCount,
          })
        ),
    });

    await loadUnits(subjectId);

    setError("");
    setSuccess("");
    setShowForm(true);
  };

  const closeForm = () => {
    if (saving) return;

    setShowForm(false);
    setEditingId(null);
    setForm(emptyForm);
    setUnits([]);
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
      constraints: [],
    }));

    setUnits([]);

    if (subjectId) {
      await loadUnits(subjectId);
    }
  };

  const addUnitConstraint = () => {
    setForm((previous) => ({
      ...previous,
      constraints: [
        ...previous.constraints,
        {
          constraintType: "UNIT",
          value: "",
          requiredCount: 1,
        },
      ],
    }));
  };

  const updateConstraint = (
    index,
    field,
    value
  ) => {
    setForm((previous) => {

      const updated = [
        ...previous.constraints,
      ];

      updated[index] = {
        ...updated[index],
        [field]: value,
      };

      return {
        ...previous,
        constraints: updated,
      };
    });
  };

  const removeConstraint = (index) => {
    setForm((previous) => ({
      ...previous,
      constraints:
        previous.constraints.filter(
          (_, constraintIndex) =>
            constraintIndex !== index
        ),
    }));
  };

  const getUsedUnitIds = () => {
    return form.constraints
      .filter(
        (constraint) =>
          constraint.constraintType ===
          "UNIT"
      )
      .map((constraint) =>
        String(constraint.value)
      );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    if (!form.subjectId) {
      setError("Please select a subject.");
      return;
    }

    if (!form.name.trim()) {
      setError("Blueprint name is required.");
      return;
    }

    if (
      Number(form.totalQuestions) <= 0
    ) {
      setError(
        "Total questions must be greater than 0."
      );
      return;
    }

    if (Number(form.totalMarks) <= 0) {
      setError(
        "Total marks must be greater than 0."
      );
      return;
    }

    const invalidConstraint =
      form.constraints.some(
        (constraint) =>
          !constraint.value ||
          Number(
            constraint.requiredCount
          ) <= 0
      );

    if (invalidConstraint) {
      setError(
        "Every constraint must have a unit and required question count."
      );
      return;
    }

    const totalRequiredQuestions =
      form.constraints.reduce(
        (sum, constraint) =>
          sum +
          Number(
            constraint.requiredCount
          ),
        0
      );

    if (
      form.constraints.length > 0 &&
      totalRequiredQuestions !==
        Number(form.totalQuestions)
    ) {
      setError(
        `Unit constraint count (${totalRequiredQuestions}) must equal total questions (${form.totalQuestions}).`
      );
      return;
    }

    try {
      setSaving(true);

      const requestData = {
        name: form.name,
        description: form.description,
        totalQuestions: Number(
          form.totalQuestions
        ),
        totalMarks: Number(
          form.totalMarks
        ),
        constraints:
          form.constraints.map(
            (constraint) => ({
              constraintType:
                constraint.constraintType,
              value: String(
                constraint.value
              ),
              requiredCount: Number(
                constraint.requiredCount
              ),
            })
          ),
      };

      if (editingId) {
        await api.put(
          `/blueprints/${editingId}`,
          requestData
        );

        setSuccess(
          "Blueprint updated successfully."
        );
      } else {
        await api.post(
          `/subjects/${form.subjectId}/blueprints`,
          requestData
        );

        setSuccess(
          "Blueprint created successfully."
        );
      }

      await loadBlueprints();

      setTimeout(() => {
        closeForm();
      }, 700);

    } catch (error) {
      console.error(
        "Blueprint save error:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to save blueprint."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmed =
      window.confirm(
        "Are you sure you want to delete this blueprint?"
      );

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setSuccess("");

      await api.delete(
        `/blueprints/${id}`
      );

      setBlueprints((previous) =>
        previous.filter(
          (blueprint) =>
            blueprint.id !== id
        )
      );

      setSuccess(
        "Blueprint deleted successfully."
      );

    } catch (error) {
      console.error(
        "Blueprint delete error:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to delete blueprint."
      );
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">

      <div className="max-w-7xl mx-auto">

        {/* Header */}

        <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4 mb-8">

          <div>
            <h1 className="text-3xl font-bold text-slate-800">
              Blueprint Builder
            </h1>

            <p className="text-slate-500 mt-1">
              Define the structure of your question paper.
            </p>
          </div>

          <button
            onClick={openCreateForm}
            className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-3 rounded-lg font-semibold"
          >
            + Create Blueprint
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

        {/* Blueprint List */}

        {loading ? (

          <div className="bg-white rounded-xl border p-10 text-center">
            Loading blueprints...
          </div>

        ) : blueprints.length === 0 ? (

          <div className="bg-white rounded-xl border p-10 text-center">

            <div className="text-5xl mb-4">
              📋
            </div>

            <h2 className="text-xl font-semibold text-slate-700">
              No blueprints found
            </h2>

            <p className="text-slate-500 mt-2">
              Create your first examination blueprint.
            </p>

          </div>

        ) : (

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

            {blueprints.map(
              (blueprint) => (

                <BlueprintCard
                  key={blueprint.id}
                  blueprint={blueprint}
                  subjects={subjects}
                  onEdit={openEditForm}
                  onDelete={handleDelete}
                />

              )
            )}

          </div>

        )}

      </div>

      {/* Modal */}

      {showForm && (
        <BlueprintFormModal
          form={form}
          editingId={editingId}
          saving={saving}
          subjects={subjects}
          units={units}
          usedUnitIds={getUsedUnitIds()}
          error={error}
          onClose={closeForm}
          onChange={handleChange}
          onSubjectChange={
            handleSubjectChange
          }
          onAddConstraint={
            addUnitConstraint
          }
          onUpdateConstraint={
            updateConstraint
          }
          onRemoveConstraint={
            removeConstraint
          }
          onSubmit={handleSubmit}
        />
      )}

    </div>
  );
};


/* Blueprint Card */

const BlueprintCard = ({
  blueprint,
  subjects,
  onEdit,
  onDelete,
}) => {

  const subject = subjects.find(
    (item) =>
      item.id === blueprint.subjectId
  );

  return (
    <div className="bg-white rounded-xl border shadow-sm p-6">

      <div className="flex justify-between items-start gap-4">

        <div>

          <h2 className="text-xl font-bold text-slate-800">
            {blueprint.name}
          </h2>

          <p className="text-sm text-blue-600 mt-1">
            {subject?.name ||
              `Subject ${blueprint.subjectId}`}
          </p>

        </div>

        <span className="bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-sm font-semibold">
          Blueprint
        </span>

      </div>

      {blueprint.description && (
        <p className="text-sm text-slate-500 mt-4">
          {blueprint.description}
        </p>
      )}

      {/* Summary */}

      <div className="grid grid-cols-2 gap-4 mt-5">

        <div className="bg-slate-50 rounded-lg p-4">

          <p className="text-xs text-slate-500">
            Questions
          </p>

          <p className="text-2xl font-bold text-slate-800 mt-1">
            {blueprint.totalQuestions}
          </p>

        </div>

        <div className="bg-slate-50 rounded-lg p-4">

          <p className="text-xs text-slate-500">
            Maximum Marks
          </p>

          <p className="text-2xl font-bold text-slate-800 mt-1">
            {blueprint.totalMarks}
          </p>

        </div>

      </div>

      {/* Constraints */}

      {blueprint.constraints &&
        blueprint.constraints.length >
          0 && (

          <div className="mt-5">

            <h3 className="font-semibold text-slate-700 mb-3">
              Constraints
            </h3>

            <div className="space-y-2">

              {blueprint.constraints.map(
                (constraint) => (

                  <div
                    key={constraint.id}
                    className="flex justify-between items-center bg-slate-50 rounded-lg px-4 py-3"
                  >

                    <span className="text-sm text-slate-600">
                      {constraint.constraintType ===
                      "UNIT"
                        ? `Unit ${constraint.value}`
                        : constraint.value}
                    </span>

                    <span className="font-semibold text-blue-600">
                      {constraint.requiredCount} questions
                    </span>

                  </div>

                )
              )}

            </div>

          </div>
        )}

      {/* Actions */}

      <div className="flex gap-3 mt-6">

        <button
          onClick={() =>
            onEdit(blueprint)
          }
          className="flex-1 border border-blue-600 text-blue-600 hover:bg-blue-50 py-2.5 rounded-lg font-medium"
        >
          Edit
        </button>

        <button
          onClick={() =>
            onDelete(blueprint.id)
          }
          className="flex-1 border border-red-500 text-red-500 hover:bg-red-50 py-2.5 rounded-lg font-medium"
        >
          Delete
        </button>

      </div>

    </div>
  );
};


/* Blueprint Form Modal */

const BlueprintFormModal = ({
  form,
  editingId,
  saving,
  subjects,
  units,
  usedUnitIds,
  error,
  onClose,
  onChange,
  onSubjectChange,
  onAddConstraint,
  onUpdateConstraint,
  onRemoveConstraint,
  onSubmit,
}) => {

  const totalRequired =
    form.constraints.reduce(
      (sum, constraint) =>
        sum +
        Number(
          constraint.requiredCount || 0
        ),
      0
    );

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">

      <div className="bg-white rounded-2xl shadow-xl w-full max-w-3xl max-h-[92vh] overflow-y-auto">

        {/* Header */}

        <div className="sticky top-0 bg-white border-b p-6 z-10">

          <div className="flex justify-between items-center">

            <div>

              <h2 className="text-2xl font-bold text-slate-800">
                {editingId
                  ? "Edit Blueprint"
                  : "Create Blueprint"}
              </h2>

              <p className="text-sm text-slate-500 mt-1">
                Define the requirements for paper generation.
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

          {/* Basic Info */}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

            <InputField
              label="Blueprint Name"
              name="name"
              value={form.name}
              onChange={onChange}
              placeholder="Machine Learning Final Exam"
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
              disabled={!!editingId}
            />

          </div>

          {/* Description */}

          <div>

            <label className="block text-sm font-medium text-slate-700 mb-2">
              Description
            </label>

            <textarea
              name="description"
              value={form.description}
              onChange={onChange}
              rows="3"
              placeholder="Describe this examination blueprint..."
              className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
            />

          </div>

          {/* Totals */}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

            <InputField
              label="Total Questions"
              name="totalQuestions"
              type="number"
              min="1"
              value={form.totalQuestions}
              onChange={onChange}
              required
            />

            <InputField
              label="Total Marks"
              name="totalMarks"
              type="number"
              min="1"
              value={form.totalMarks}
              onChange={onChange}
              required
            />

          </div>

          {/* Unit Constraints */}

          <div className="border rounded-xl p-5 bg-slate-50">

            <div className="flex justify-between items-start gap-4 mb-5">

              <div>

                <h3 className="font-bold text-slate-800">
                  Unit Distribution
                </h3>

                <p className="text-sm text-slate-500 mt-1">
                  Specify how many questions should come from each unit.
                </p>

              </div>

              <button
                type="button"
                onClick={onAddConstraint}
                disabled={
                  units.length === 0
                }
                className="bg-blue-600 hover:bg-blue-700 disabled:bg-slate-300 text-white px-3 py-2 rounded-lg text-sm font-semibold"
              >
                + Add Unit
              </button>

            </div>

            {form.constraints.length ===
            0 ? (

              <div className="text-center border border-dashed border-slate-300 rounded-lg p-6 text-slate-500">
                No unit constraints added yet.
              </div>

            ) : (

              <div className="space-y-3">

                {form.constraints.map(
                  (constraint, index) => (

                    <div
                      key={index}
                      className="flex flex-col md:flex-row gap-3 items-end bg-white border rounded-lg p-4"
                    >

                      <div className="flex-1 w-full">

                        <label className="block text-xs font-medium text-slate-500 mb-1">
                          Unit
                        </label>

                        <select
                          value={
                            constraint.value
                          }
                          onChange={(e) =>
                            onUpdateConstraint(
                              index,
                              "value",
                              e.target.value
                            )
                          }
                          className="w-full border border-slate-300 rounded-lg px-3 py-2.5"
                          required
                        >

                          <option value="">
                            Select Unit
                          </option>

                          {units
                            .filter(
                              (unit) =>
                                !usedUnitIds.includes(
                                  String(unit.id)
                                ) ||
                                String(
                                  constraint.value
                                ) ===
                                  String(
                                    unit.id
                                  )
                            )
                            .map((unit) => (

                              <option
                                key={unit.id}
                                value={unit.id}
                              >
                                Unit{" "}
                                {
                                  unit.unitNumber
                                }{" "}
                                -{" "}
                                {unit.title}
                              </option>

                            ))}

                        </select>

                      </div>

                      <div className="w-full md:w-40">

                        <label className="block text-xs font-medium text-slate-500 mb-1">
                          Questions
                        </label>

                        <input
                          type="number"
                          min="1"
                          value={
                            constraint.requiredCount
                          }
                          onChange={(e) =>
                            onUpdateConstraint(
                              index,
                              "requiredCount",
                              e.target.value
                            )
                          }
                          className="w-full border border-slate-300 rounded-lg px-3 py-2.5"
                          required
                        />

                      </div>

                      <button
                        type="button"
                        onClick={() =>
                          onRemoveConstraint(
                            index
                          )
                        }
                        className="text-red-500 hover:bg-red-50 px-3 py-2.5 rounded-lg"
                      >
                        Remove
                      </button>

                    </div>

                  )
                )}

              </div>

            )}

            {/* Distribution total */}

            <div className="mt-5 flex justify-between items-center bg-white border rounded-lg px-4 py-3">

              <span className="text-sm text-slate-600">
                Required questions from units
              </span>

              <span
                className={`font-bold ${
                  totalRequired ===
                  Number(
                    form.totalQuestions
                  )
                    ? "text-green-600"
                    : "text-red-500"
                }`}
              >
                {totalRequired} /{" "}
                {form.totalQuestions}
              </span>

            </div>

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
                ? "Update Blueprint"
                : "Create Blueprint"}
            </button>

          </div>

        </form>

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
  disabled = false,
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
        disabled={disabled}
        className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500 bg-white disabled:bg-slate-100"
      >

        <option value="">
          Select {label}
        </option>

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

export default Blueprints;
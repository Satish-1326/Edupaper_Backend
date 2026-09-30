import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

const emptyForm = {
  name: "",
  code: "",
  department: "",
  semester: "",
  academicYear: "",
  credits: "",
  description: "",
};

const Subjects = () => {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);

    const navigate = useNavigate();

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState(emptyForm);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadSubjects();
  }, []);

  const loadSubjects = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/subjects");

      setSubjects(response.data);
    } catch (error) {
      console.error("Failed to load subjects:", error);

      setError(error.response?.data?.message || "Failed to load subjects");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const openAddForm = () => {
    setEditingId(null);
    setForm(emptyForm);
    setError("");
    setSuccess("");
    setShowForm(true);
  };

  const openEditForm = (subject) => {
    setEditingId(subject.id);

    setForm({
      name: subject.name || "",
      code: subject.code || "",
      department: subject.department || "",
      semester: subject.semester || "",
      academicYear: subject.academicYear || "",
      credits: subject.credits || "",
      description: subject.description || "",
    });

    setError("");
    setSuccess("");
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    try {
      const requestData = {
        name: form.name,
        code: form.code,
        department: form.department,
        semester: Number(form.semester),
        academicYear: form.academicYear,
        credits: Number(form.credits),
        description: form.description,
      };

      if (editingId) {
        await api.put(`/subjects/${editingId}`, requestData);

        setSuccess("Subject updated successfully");
      } else {
        await api.post("/subjects", requestData);

        setSuccess("Subject created successfully");
      }

      await loadSubjects();

      setTimeout(() => {
        closeForm();
      }, 700);
    } catch (error) {
      console.error("Subject save error:", error);

      setError(error.response?.data?.message || "Failed to save subject");
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this subject?",
    );

    if (!confirmed) {
      return;
    }

    try {
      setError("");

      await api.delete(`/subjects/${id}`);

      setSuccess("Subject deleted successfully");

      await loadSubjects();
    } catch (error) {
      console.error("Subject delete error:", error);

      setError(error.response?.data?.message || "Failed to delete subject");
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      {/* Header */}

      <div className="max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-slate-800">Subjects</h1>

            <p className="text-slate-500 mt-1">
              Manage your subjects and course structure.
            </p>
          </div>

          <button
            onClick={openAddForm}
            className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-3 rounded-lg font-semibold"
          >
            + Add Subject
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

        {/* Loading */}

        {loading ? (
          <div className="bg-white rounded-xl p-10 text-center shadow-sm">
            <p className="text-slate-500">Loading subjects...</p>
          </div>
        ) : subjects.length === 0 ? (
          <div className="bg-white rounded-xl p-10 text-center shadow-sm">
            <div className="text-5xl mb-4">📚</div>

            <h2 className="text-xl font-semibold text-slate-700">
              No subjects found
            </h2>

            <p className="text-slate-500 mt-2">
              Create your first subject to get started.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {subjects.map((subject) => (
              <div
                key={subject.id}
                onClick={() => navigate(`/subjects/${subject.id}`)}
                className="bg-white rounded-xl shadow-sm border p-6 hover:shadow-md hover:border-blue-400 transition cursor-pointer"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-xl font-bold text-slate-800">
                      {subject.name}
                    </h2>

                    <p className="text-blue-600 font-medium mt-1">
                      {subject.code}
                    </p>
                  </div>

                  <span className="bg-blue-50 text-blue-600 text-xs font-semibold px-3 py-1 rounded-full">
                    {subject.credits} Credits
                  </span>
                </div>

                <div className="mt-5 space-y-2 text-sm text-slate-600">
                  <p>
                    <span className="font-medium">Department:</span>{" "}
                    {subject.department}
                  </p>

                  <p>
                    <span className="font-medium">Semester:</span>{" "}
                    {subject.semester}
                  </p>

                  <p>
                    <span className="font-medium">Academic Year:</span>{" "}
                    {subject.academicYear}
                  </p>
                </div>

                {subject.description && (
                  <p className="mt-4 text-sm text-slate-500 line-clamp-2">
                    {subject.description}
                  </p>
                )}

                <div className="flex gap-3 mt-6">
                  <button
                    onClick={() => openEditForm(subject)}
                    className="flex-1 border border-blue-600 text-blue-600 hover:bg-blue-50 py-2 rounded-lg font-medium"
                  >
                    Edit
                  </button>

                  <button
                    onClick={() => handleDelete(subject.id)}
                    className="flex-1 border border-red-500 text-red-500 hover:bg-red-50 py-2 rounded-lg font-medium"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add / Edit Modal */}

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b">
              <div className="flex justify-between items-center">
                <div>
                  <h2 className="text-2xl font-bold text-slate-800">
                    {editingId ? "Edit Subject" : "Create Subject"}
                  </h2>

                  <p className="text-sm text-slate-500 mt-1">
                    Enter the subject details.
                  </p>
                </div>

                <button
                  onClick={closeForm}
                  className="text-slate-400 hover:text-slate-700 text-2xl"
                >
                  ×
                </button>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-5">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <Input
                  label="Subject Name"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Machine Learning"
                  required
                />

                <Input
                  label="Subject Code"
                  name="code"
                  value={form.code}
                  onChange={handleChange}
                  placeholder="CS501"
                  required
                />

                <Input
                  label="Department"
                  name="department"
                  value={form.department}
                  onChange={handleChange}
                  placeholder="Computer Science"
                  required
                />

                <Input
                  label="Semester"
                  name="semester"
                  type="number"
                  value={form.semester}
                  onChange={handleChange}
                  placeholder="7"
                  required
                />

                <Input
                  label="Academic Year"
                  name="academicYear"
                  value={form.academicYear}
                  onChange={handleChange}
                  placeholder="2026-27"
                  required
                />

                <Input
                  label="Credits"
                  name="credits"
                  type="number"
                  value={form.credits}
                  onChange={handleChange}
                  placeholder="4"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Description
                </label>

                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  rows="4"
                  placeholder="Enter subject description..."
                  className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {error && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
                  {error}
                </div>
              )}

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={closeForm}
                  className="px-5 py-2.5 border border-slate-300 rounded-lg text-slate-700"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-semibold"
                >
                  {editingId ? "Update Subject" : "Create Subject"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

/* Reusable Input */

const Input = ({
  label,
  name,
  type = "text",
  value,
  onChange,
  placeholder,
  required,
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
        className="w-full border border-slate-300 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  );
};

export default Subjects;

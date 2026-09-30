import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [stats, setStats] = useState({
    subjects: 0,
    questions: 0,
    blueprints: 0,
    papers: 0,
  });

  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const [
        subjectsResponse,
        questionsResponse,
        blueprintsResponse,
        papersResponse,
      ] = await Promise.all([
        api.get("/subjects"),
        api.get("/questions"),
        api.get("/blueprints"),
        api.get("/papers"),
      ]);

      const subjects = subjectsResponse.data;
      const questions = questionsResponse.data;
      const blueprints = blueprintsResponse.data;
      const papersData = papersResponse.data;

      setStats({
        subjects: subjects.length,
        questions: questions.length,
        blueprints: blueprints.length,
        papers: papersData.length,
      });

      setPapers(papersData.slice(0, 5));
    } catch (error) {
      console.error("Dashboard loading error:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100">
      {/* Navbar */}
      <nav className="bg-white border-b px-8 py-4 flex justify-between items-center">
        <div>
          <h1 className="text-xl font-bold text-blue-600">EduPaper AI</h1>

          <p className="text-xs text-slate-500">
            Question Paper Generation System
          </p>
        </div>

        <div className="flex items-center gap-5">
          <div className="text-right">
            <p className="font-semibold text-slate-700">{user?.name}</p>

            <p className="text-xs text-slate-500">{user?.role}</p>
          </div>

          <button
            onClick={logout}
            className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg"
          >
            Logout
          </button>
        </div>
      </nav>

      {/* Main */}
      <main className="p-8 max-w-7xl mx-auto">
        {/* Welcome */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-slate-800">
            Welcome back, {user?.name} 👋
          </h2>

          <p className="text-slate-500 mt-2">
            Manage your subjects, questions and examination papers.
          </p>
        </div>

        {/* Statistics */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {/* Subjects */}
          <StatCard
            title="Subjects"
            value={loading ? "..." : stats.subjects}
            icon="📚"
            onClick={() => navigate("/subjects")}
          />

          {/* Questions */}
          <StatCard
            title="Questions"
            value={loading ? "..." : stats.questions}
            icon="❓"
            onClick={() => navigate("/questions")}
          />

          {/* Blueprints */}
          <StatCard
            title="Blueprints"
            value={loading ? "..." : stats.blueprints}
            icon="📋"
            onClick={() => navigate("/blueprints")}
          />

          {/* Papers */}
          <StatCard
            title="Papers"
            value={loading ? "..." : stats.papers}
            icon="📄"
            onClick={() => navigate("/papers")}
          />
        </div>

        {/* Recent Papers */}
        <div className="mt-10">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-bold text-slate-800">Recent Papers</h3>
          </div>

          <div className="bg-white rounded-xl shadow-sm border">
            {loading ? (
              <div className="p-8 text-center text-slate-500">
                Loading papers...
              </div>
            ) : papers.length === 0 ? (
              <div className="p-8 text-center text-slate-500">
                No papers generated yet.
              </div>
            ) : (
              <div className="divide-y">
                {papers.map((paper) => (
                  <div
                    key={paper.id}
                    className="p-5 flex justify-between items-center"
                  >
                    <div>
                      <h4 className="font-semibold text-slate-800">
                        {paper.name}
                      </h4>

                      <p className="text-sm text-slate-500 mt-1">
                        {paper.totalQuestions} Questions
                        {" • "}
                        {paper.totalMarks} Marks
                      </p>
                    </div>

                    <span className="px-3 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">
                      {paper.status}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

/* Statistics Card */

const StatCard = ({ title, value, icon, onClick }) => {
  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-xl shadow-sm border p-6 transition
        ${
          onClick
            ? "cursor-pointer hover:shadow-md hover:border-blue-400 hover:-translate-y-1"
            : ""
        }`}
    >
      <div className="flex justify-between items-start">
        <div>
          <p className="text-slate-500 text-sm">{title}</p>

          <h3 className="text-3xl font-bold text-slate-800 mt-2">{value}</h3>
        </div>

        <div className="text-3xl">{icon}</div>
      </div>

      {onClick && <p className="text-xs text-blue-500 mt-4">Click to view →</p>}
    </div>
  );
};

export default Dashboard;

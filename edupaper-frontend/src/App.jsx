import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import { AuthProvider } from "./context/AuthContext";

import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Subjects from "./pages/Subjects";
import ProtectedRoute from "./components/ProtectedRoute";
import SubjectDetails from "./pages/SubjectDetails";
import Questions from "./pages/Questions";
import Blueprints from "./pages/Blueprints";
import Papers from "./pages/Papers";
import PaperEditor from "./pages/PaperEditor";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Login */}
          <Route path="/login" element={<Login />} />

          {/* Dashboard */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          {/* Subjects */}
          <Route
            path="/subjects"
            element={
              <ProtectedRoute>
                <Subjects />
              </ProtectedRoute>
            }
          />

          {/* Root */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* Unknown URL */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />

          <Route
            path="/subjects/:subjectId"
            element={
              <ProtectedRoute>
                <SubjectDetails />
              </ProtectedRoute>
            }
          />

          <Route
            path="/questions"
            element={
              <ProtectedRoute>
                <Questions />
              </ProtectedRoute>
            }
          />

          <Route
            path="/blueprints"
            element={
              <ProtectedRoute>
                <Blueprints />
              </ProtectedRoute>
            }
          />

          <Route
            path="/papers"
            element={
              <ProtectedRoute>
                <Papers />
              </ProtectedRoute>
            }
          />

          <Route
            path="/papers/:paperId"
            element={
              <ProtectedRoute>
                <PaperEditor />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

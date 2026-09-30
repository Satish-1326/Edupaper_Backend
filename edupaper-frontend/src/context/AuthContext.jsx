import { createContext, useContext, useState } from "react";
import api from "../services/api";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const storedUser = localStorage.getItem("user");
    return storedUser ? JSON.parse(storedUser) : null;
  });

  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);

    try {
      const response = await api.post("/auth/login", {
        email,
        password,
      });

      const data = response.data;

      /*
       * Your backend login response contains the JWT.
       * If your exact response uses a different field name,
       * we'll adjust it after testing.
       */
      const token = data.token;

      localStorage.setItem("token", token);

      const loggedInUser = {
        id: data.id,
        name: data.name,
        email: data.email,
        role: data.role,
      };

      localStorage.setItem("user", JSON.stringify(loggedInUser));

      setUser(loggedInUser);

      return {
        success: true,
        data,
      };
    } catch (error) {
      console.error("Login error:", error);

      return {
        success: false,
        message:
          error.response?.data?.message ||
          "Invalid email or password",
      };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};
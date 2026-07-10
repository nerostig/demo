// src/auth/AuthProvider.tsx
import React, { createContext, useContext, useReducer, useEffect } from "react";
import { Navigate, useLocation } from "react-router-dom";

interface User {
    id: string;
    username: string;
}

interface AuthState {
    user: User | null;
    loading: boolean;
}

type AuthAction =
    | { type: "LOGIN"; user: User }
    | { type: "LOGOUT" }
    | { type: "SET_LOADING"; value: boolean };

const AuthContext = createContext<{
    state: AuthState;
    dispatch: React.Dispatch<AuthAction>;
} | null>(null);

const initialState: AuthState = {
    user: null,
    loading: true,
};

function authReducer(state: AuthState, action: AuthAction): AuthState {
    switch (action.type) {
        case "LOGIN":
            return { ...state, user: action.user, loading: false };
        case "LOGOUT":
            return { ...state, user: null, loading: false };
        case "SET_LOADING":
            return { ...state, loading: action.value };
        default:
            return state;
    }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [state, dispatch] = useReducer(authReducer, initialState);
    const location = useLocation();

    useEffect(() => {
        fetch("/api/me", {
            credentials: "include",
        })
            .then((res) => {
                if (res.ok) return res.json();
                throw new Error("Not authenticated");
            })
            .then((data) => {
                dispatch({ type: "LOGIN", user: data });
            })
            .catch(() => {
                dispatch({ type: "LOGOUT" });
            });
    }, []);

    if (state.loading) return <div>Carregando...</div>;

    if (!state.user &&
        !["/login", "/register"].includes(location.pathname)) {
        return <Navigate to="/login" replace />;
    }

    if (state.user &&
        ["login", "register"].includes(location.pathname.split("/")[1])) {
        return <Navigate to="/" replace />;
    }

    return (
        <AuthContext.Provider value={{ state, dispatch }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth tem que ser identificado com AuthProvider");
    }
    return context;
}

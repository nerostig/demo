// src/pages/Login.tsx
import React, { useState } from "react";
import {Link, useNavigate } from "react-router-dom";
import {useAuth} from "../layout/Auth/ AuthProvider";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const { dispatch } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const res = await fetch("/api/users/token", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
                credentials: "include",
            });

            if (res.ok) {
                const userRes = await fetch("/api/me", {
                    credentials: "include",
                });
                const userData = await userRes.json();
                dispatch({ type: "LOGIN", user: userData });
                navigate("/");
            } else {
                alert("Credenciais inválidas");
            }
        } catch (error) {
            console.error("Erro no login:", error);
        }
    };

    return (
        <div>
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <input
                    className="border rounded px-2 py-1 text-black"
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <input
                    className="border rounded px-2 py-1 text-black"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button type="submit">Login</button>
                <p>
                    Não tens conta? <Link to="/register">Registar</Link>
                </p>
            </form>
        </div>
    );
}

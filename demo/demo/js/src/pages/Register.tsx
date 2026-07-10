// src/pages/Register.tsx
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const res = await fetch("/api/users", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            if (res.status === 201) {
                alert("Registo realizado com sucesso!");
                navigate("/login");
            } else {
                alert("Erro ao registar user");
            }
        } catch (error) {
            console.error("Erro no registo:", error);
        }
    };

    return (
        <div>
            <h2>Registo</h2>
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
                <button type="submit">Registar</button>
            </form>
        </div>
    );
}

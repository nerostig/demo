
import React from "react";
import {useAuth} from "../layout/Auth/ AuthProvider";

export function LogoutButton() {
    const { dispatch } = useAuth();

    function logout() {
        fetch("/api/logout", {
            method: "POST",
        }).then(() => {
            dispatch({ type: "LOGOUT" });
        });
    }

    return <button onClick={logout}>Logout</button>;
}

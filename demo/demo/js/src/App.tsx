import { Toaster } from "./layout/components/ui/toaster";
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import AppLayout from "./layout/components/layout/AppLayout";
import Dashboard from "./pages/Dashboard";
import Networks from "./pages/Networks";
import NetworkEditor from "./pages/NetworkEditor";
import About from "./pages/About";
import React from "react";
import Register from "./pages/Register";
import Login from "./pages/Login";



const App = () => {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route element={<AppLayout />}>
                <Route path="/" element={<Dashboard />} />
                <Route path="/networks" element={<Networks />} />
                <Route path="/editor/:id" element={<NetworkEditor />} />
                <Route path="/about" element={<About />} />
            </Route>
        </Routes>
    );
};



export default App
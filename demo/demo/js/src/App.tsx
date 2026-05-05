import { Toaster } from "./layout/components/ui/toaster";
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import AppLayout from "./layout/components/layout/AppLayout";
import Dashboard from "./pages/Dashboard";
import Networks from "./pages/Networks";
import NetworkEditor from "./pages/NetworkEditor";
import About from "./pages/About";
import React from "react";



const App = () => {
    return (
        <Router>
            <Routes>
                <Route element={<AppLayout />}>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/networks" element={<Networks />} />
                    <Route path="/editor/:id" element={<NetworkEditor />} />
                    <Route path="/about" element={<About />} />
                </Route>

                {/*<Route path="*" element={<PageNotFound />} />*/}
            </Routes>

            <Toaster />
        </Router>
    )
}

export default App
import React, {JSX} from 'react'
import {Link, Outlet} from 'react-router-dom'
import Sidebar from './Sidebar'
import MobileNav from './MobileNav'
import {useAuth} from "../../Auth/ AuthProvider";
import {LogoutButton} from "../../../pages/Logout";

export default function AppLayout() {
    const { state } = useAuth();
    const { user } = state;

    return (
        <div className={user ? "flex min-h-screen bg-background" : ""}>

            {/* Sidebar  */}
            {user && <Sidebar />}

            <div className={user ? "flex-1 flex flex-col min-h-screen" : ""}>

                {/* Nav  */}
                <nav>
                    <Link to="/">Home</Link>

                    {!user && <Link to="/register">Register</Link>}
                    {!user && <Link to="/login">Login</Link>}
                    {user && <Link to="/networks">Networks</Link>}
                </nav>


                {user && <MobileNav />}
                {user && <LogoutButton  />}



                <main className="flex-1 overflow-auto">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}


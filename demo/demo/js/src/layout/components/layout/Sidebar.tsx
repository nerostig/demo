import React, {JSX} from 'react'
import { Link, useLocation } from 'react-router-dom'
import { LayoutDashboard, Network, Plus, Info, Radio } from 'lucide-react'
import { LucideIcon } from 'lucide-react'
import {ClassValue, clsx} from "clsx";
import {twMerge} from "tailwind-merge";


export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}
/* ---------- Types ---------- */

interface NavItem {
    path: string
    icon: LucideIcon
    label: string
}

/* ---------- Data ---------- */

const navItems: NavItem[] = [
    { path: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { path: '/networks', icon: Network, label: 'Redes' },
    { path: '/editor/new', icon: Plus, label: 'Nova Rede' },
    { path: '/about', icon: Info, label: 'Sobre' },
]

/* ---------- Component ---------- */

export default function Sidebar(): JSX.Element {
    const location = useLocation()

    return (
        <aside className="hidden lg:flex flex-col w-64 bg-sidebar text-sidebar-foreground border-r border-sidebar-border min-h-screen">

            {/* Logo */}
            <div className="flex items-center gap-3 px-6 py-5 border-b border-sidebar-border">
                <div className="w-9 h-9 rounded-lg bg-primary/20 flex items-center justify-center">
                    <Radio className="w-5 h-5 text-primary" />
                </div>

                <div>
                    <h1 className="text-sm font-semibold text-sidebar-primary-foreground">
                        DutyCycle
                    </h1>
                    <p className="text-xs text-sidebar-foreground/50">
                        Sensor Networks
                    </p>
                </div>
            </div>

            {/* Navigation */}
            <nav className="flex-1 px-3 py-4 space-y-1">
                {navItems.map((item: NavItem) => {
                    const isActive =
                        location.pathname === item.path ||
                        (item.path !== '/' &&
                            location.pathname.startsWith(item.path))

                    return (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={cn(
                                "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all duration-200",
                                isActive
                                    ? "bg-sidebar-accent text-sidebar-primary font-medium"
                                    : "text-sidebar-foreground/70 hover:text-sidebar-foreground hover:bg-sidebar-accent/50"
                            )}
                        >
                            <item.icon className="w-4 h-4" />
                            {item.label}
                        </Link>
                    )
                })}
            </nav>

            {/* Footer */}
            <div className="px-4 py-4 border-t border-sidebar-border">
                <p className="text-xs text-sidebar-foreground/40 text-center">
                    Planeamento de Ciclo de Trabalho
                </p>
            </div>
        </aside>
    )
}
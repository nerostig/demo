import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Network, Plus, Info, Radio, Menu, X } from 'lucide-react';
import { Sheet, SheetContent, SheetTrigger } from "../ui/sheet";
import {Button} from "../ui/button";
import {ClassValue, clsx} from "clsx";
import {twMerge} from "tailwind-merge";
export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}
const navItems = [
    { path: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { path: '/networks', icon: Network, label: 'Redes' },
    { path: '/editor/new', icon: Plus, label: 'Nova Rede' },
    { path: '/about', icon: Info, label: 'Sobre' },
];

export default function MobileNav() {
    const [open, setOpen] = useState(false);
    const location = useLocation();

    return (
        <div className="lg:hidden flex items-center justify-between px-4 py-3 bg-sidebar border-b border-sidebar-border">
            <div className="flex items-center gap-2">
                <Radio className="w-5 h-5 text-primary" />
                <span className="text-sm font-semibold text-white">DutyCycle</span>
            </div>
            <Sheet open={open} onOpenChange={setOpen}>
                <SheetTrigger asChild>
                    <Button variant="ghost" size="icon" className="text-sidebar-foreground">
                        <Menu className="w-5 h-5" />
                    </Button>
                </SheetTrigger>
                <SheetContent side="left" className="bg-sidebar border-sidebar-border w-64 p-0">
                    <div className="px-6 py-5 border-b border-sidebar-border">
                        <div className="flex items-center gap-2">
                            <Radio className="w-5 h-5 text-primary" />
                            <span className="text-sm font-semibold text-white">DutyCycle</span>
                        </div>
                    </div>
                    <nav className="px-3 py-4 space-y-1">
                        {navItems.map(item => {
                            const isActive = location.pathname === item.path;
                            return (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    onClick={() => setOpen(false)}
                                    className={cn(
                                        "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all",
                                        isActive
                                            ? "bg-sidebar-accent text-sidebar-primary font-medium"
                                            : "text-sidebar-foreground/70 hover:text-sidebar-foreground hover:bg-sidebar-accent/50"
                                    )}
                                >
                                    <item.icon className="w-4 h-4" />
                                    {item.label}
                                </Link>
                            );
                        })}
                    </nav>
                </SheetContent>
            </Sheet>
        </div>
    );
}
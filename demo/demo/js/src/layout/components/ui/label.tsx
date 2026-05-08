import * as React from "react"
import * as LabelPrimitive from "@radix-ui/react-label"
import { cva } from "class-variance-authority"
import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

// utilitário de classes
export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}

// SSR-safe
export const isIframe: boolean =
    typeof window !== "undefined" && window.self !== window.top

const labelVariants = cva(
    "text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
)

export interface LabelProps
    extends React.ComponentPropsWithoutRef<typeof LabelPrimitive.Root> {
    className?: string
}

const Label = React.forwardRef<
    React.ElementRef<typeof LabelPrimitive.Root>,
    LabelProps
>(({ className, ...props }, ref) => (
    <LabelPrimitive.Root
        ref={ref}
        className={cn(labelVariants(), className)}
        {...props}
    />
))

Label.displayName = LabelPrimitive.Root.displayName

export { Label }
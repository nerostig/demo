import * as React from "react"
import { useToast } from "./use-toast"
import {
    Toast,
    ToastClose,
    ToastDescription,
    ToastProvider,
    ToastTitle,
    ToastViewport,
} from "./toast"

type ToastAction = React.ReactNode

type ToastItem = {
    id: string
    title?: React.ReactNode
    description?: React.ReactNode
    action?: ToastAction
    [key: string]: any
}

export function Toaster() {
    const { toasts } = useToast()

    return (
        <ToastProvider>
            {toasts.map(
                ({ id, title, description, action, ...props }: ToastItem) => {
                    return (
                        <Toast key={id} {...props}>
                            <div className="grid gap-1">
                                {title && <ToastTitle>{title}</ToastTitle>}
                                {description && (
                                    <ToastDescription>{description}</ToastDescription>
                                )}
                            </div>

                            {action}

                            <ToastClose />
                        </Toast>
                    )
                }
            )}

            <ToastViewport />
        </ToastProvider>
    )
}
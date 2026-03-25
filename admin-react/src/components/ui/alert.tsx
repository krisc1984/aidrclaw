import * as React from "react";
import { cn } from "@/lib/utils";

export interface AlertProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'destructive';
}

const Alert: React.FC<AlertProps> = ({ className, variant = 'default', ...props }) => {
  return (
    <div
      className={cn(
        "relative w-full rounded-lg border p-4",
        {
          'bg-background text-foreground': variant === 'default',
          'border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive': variant === 'destructive',
        },
        className
      )}
      {...props}
    />
  );
};

const AlertTitle: React.FC<{ children: React.ReactNode; className?: string }> = ({ children, className }) => {
  return <h5 className={cn("mb-1 font-medium leading-none tracking-tight", className)}>{children}</h5>;
};

const AlertDescription: React.FC<{ children: React.ReactNode; className?: string }> = ({ children, className }) => {
  return <div className={cn("text-sm [&_p]:leading-relaxed", className)}>{children}</div>;
};

export { Alert, AlertTitle, AlertDescription };

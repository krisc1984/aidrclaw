import * as React from "react";
import { cn } from "@/lib/utils";

const Select: React.FC<{ children: React.ReactNode; value?: string; onValueChange?: (value: string) => void; className?: string }> = ({ children, value, onValueChange, className }) => {
  return <div className={cn("relative", className)}>{children}</div>;
};

const SelectTrigger = React.forwardRef<HTMLButtonElement, React.ButtonHTMLAttributes<HTMLButtonElement>>(
  ({ className, children, ...props }, ref) => (
    <button ref={ref} className={cn("flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm", className)} {...props}>
      {children}
    </button>
  )
);
SelectTrigger.displayName = "SelectTrigger";

const SelectContent = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, children, ...props }, ref) => (
    <div ref={ref} className={cn("absolute z-50 min-w-[8rem] overflow-hidden rounded-md border bg-white shadow-md mt-1", className)} {...props}>
      {children}
    </div>
  )
);
SelectContent.displayName = "SelectContent";

const SelectItem: React.FC<{ children: React.ReactNode; value: string; className?: string }> = ({ children, value, className }) => {
  return <div className={cn("relative flex w-full cursor-pointer select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none hover:bg-gray-100", className)} data-value={value}>{children}</div>;
};

const SelectValue: React.FC<{ placeholder: string; className?: string }> = ({ placeholder, className }) => {
  return <span className={cn("text-sm text-gray-600", className)}>{placeholder}</span>;
};

export { Select, SelectTrigger, SelectContent, SelectItem, SelectValue };

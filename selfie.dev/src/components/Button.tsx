import clsx from "clsx/lite";
import { ReactNode } from "react";

type ButtonProps = {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
  onMouseEnter?: () => void;
  onMouseLeave?: () => void;
  onTouchStart?: () => void;
};

export function Button({
  children,
  className,
  onClick,
  onMouseEnter,
  onMouseLeave,
  onTouchStart,
}: ButtonProps) {
  return (
    <span
      className={clsx(buttonClasses, className)}
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      onTouchStart={onTouchStart}
      role="button"
    >
      {children}
    </span>
  );
}
type ButtonLinkProps = {
  children: ReactNode;
  className?: string;
  href: string;
};
export function ButtonLink({ children, className, href }: ButtonLinkProps) {
  return (
    <a className={clsx(buttonClasses, className)} href={href} role="button">
      {children}
    </a>
  );
}

const buttonClasses = clsx(
  "flex",
  "justify-center",
  "items-center",
  "border",
  "border-2",
  "border-black",
  "cursor-pointer",
  "tablet:h-[35px]",
  "tablet:text-[22px]",
  "tablet:border-[3px]",
  "tablet:rounded-[10px]",
  "desktop:h-[53px]",
  "desktop:text-[34px]",
  "desktop:border-[4px]",
  "desktop:rounded-[16px]"
);

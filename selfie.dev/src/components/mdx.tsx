import clsx from "clsx/lite";
import { createContext, useContext } from "react";
import { LinkIcon } from "./Icons/LinkIcon";
import slugify from "@sindresorhus/slugify";
import { HeadingAnchor } from "./HeadingAnchor";

type ParentComponentProps = {
  children?: React.ReactNode;
};

export function Row({ children }: ParentComponentProps) {
  return (
    <div className="grid grid-cols-1 items-start gap-x-16 gap-y-10 xl:max-w-none xl:grid-cols-2">
      {children}
    </div>
  );
}

type ColProps = ParentComponentProps & {
  sticky?: boolean;
};

export function Col({ children, sticky = false }: ColProps) {
  return (
    <div
      className={clsx(
        "[&>:first-child]:mt-0 [&>:last-child]:mb-0",
        sticky && "xl:sticky xl:top-24"
      )}
    >
      {children}
    </div>
  );
}

const CodeBlockContext = createContext(false);

export function code({ children, ...props }: ParentComponentProps) {
  let isBlock = useContext(CodeBlockContext);
  const className = isBlock
    ? ""
    : clsx(
        "bg-grey",
        "px-[7px]",
        "py-[2px]",
        "rounded",
        "text-sm",
        "leading-[1.5em]",
        "break-words"
      );
  return children ? (
    <code
      {...props}
      className={className}
      dangerouslySetInnerHTML={{ __html: children }}
    />
  ) : (
    <code {...props} className={className} />
  );
}

export function pre({ children, ...props }: ParentComponentProps) {
  return (
    <CodeBlockContext.Provider value={true}>
      <div
        className={clsx(
          "rounded-lg",
          "bg-grey/60",
          "shadow",
          "text-sm",
          "overflow-hidden",
          "leading-[1.5em]"
        )}
      >
        <pre className="overflow-scroll p-2" {...props}>
          {children}
        </pre>
      </div>
    </CodeBlockContext.Provider>
  );
}

export function p({ children, ...props }: ParentComponentProps) {
  return (
    <p {...props} className="py-[13px]">
      {children}
    </p>
  );
}

export function h2({ children, ...props }: ParentComponentProps) {
  const slug = typeof children === "string" ? slugify(children) : "";
  return (
    <>
      <br />
      <h2 {...props} className="group flex items-center">
        {children} <HeadingAnchor slug={slug} />
      </h2>
    </>
  );
}

export function h3({ children, ...props }: ParentComponentProps) {
  const slug = typeof children === "string" ? slugify(children) : "";
  return (
    <>
      <h3 {...props} className="group flex items-center text-lg">
        {children} <HeadingAnchor slug={slug} />
      </h3>
    </>
  );
}

export function a({ children, ...props }: ParentComponentProps) {
  return (
    <a
      {...props}
      className="underline hover:text-blue visited:hover:text-purple"
    >
      {children}
    </a>
  );
}

export function ul({ children, ...props }: ParentComponentProps) {
  return (
    <ul {...props} className="list-inside list-disc">
      {children}
    </ul>
  );
}

export function li({ children, ...props }: ParentComponentProps) {
  return (
    <li {...props}>
      <span className="-ml-3">{children}</span>
    </li>
  );
}

declare global {
  type ValueOf<T> = T[keyof T];
  type TAnyFunc<P extends any[] = any[], T extends any = any> = (
    ...args: P
  ) => T;
}

export {};

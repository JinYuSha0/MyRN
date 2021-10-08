export interface Component {
  BundleName: string;
  ComponentName: string;
  Version: number;
  Hash: string;
  FilePath: string;
  PublishTime: number;
  InstallTime: number;
}

export interface CheckUpdateResult {
  data: {
    version: number;
    hash: string;
    commonHash: string;
    isCommon: boolean;
    componentName: string;
    downloadUrl: string;
    buildTime: number;
  }[];
  code: number;
  success: boolean;
}

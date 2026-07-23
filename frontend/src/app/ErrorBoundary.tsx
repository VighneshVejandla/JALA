import { Component, type ErrorInfo, type ReactNode } from 'react';
import { ServerError } from '@/features/misc/ServerError';

interface Props {
  children: ReactNode;
}
interface State {
  hasError: boolean;
}

/** Catches render-time crashes and shows the 500 screen instead of a blank page. */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    // Surface to the console for diagnostics; a real deploy would report this.
    console.error('Unhandled UI error:', error, info.componentStack);
  }

  reset = () => this.setState({ hasError: false });

  render() {
    if (this.state.hasError) {
      return <ServerError onReset={this.reset} />;
    }
    return this.props.children;
  }
}
